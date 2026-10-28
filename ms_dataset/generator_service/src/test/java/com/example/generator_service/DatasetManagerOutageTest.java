package com.example.generator_service;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

/**
 * The generator against a fake dataset-manager (a JDK HTTP server), with the real resilience settings from
 * config-repo/generator.properties. Covers the brief's validation scenario: when the manager fails, the generator
 * answers with its fallback, never a stack trace, and never an empty file passed off as an export.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
})
@TestPropertySource(locations = "file:../config-repo/generator.properties")
class DatasetManagerOutageTest {

    private static final HttpServer MANAGER = fakeManager();

    @LocalServerPort
    private int port;
    @Autowired
    private CircuitBreakerRegistry circuitBreakers;

    private RestClient generator;

    @DynamicPropertySource
    static void managerAddress(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.discovery.client.simple.instances.dataset-manager[0].uri",
                () -> "http://localhost:" + MANAGER.getAddress().getPort());
    }

    @BeforeEach
    void setUp() {
        generator = RestClient.builder().baseUrl("http://localhost:" + port + "/api/generator")
                .defaultStatusHandler(status -> true, (request, response) -> { })
                .build();
        circuitBreakers.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
    }

    @AfterAll
    static void stopManager() {
        MANAGER.stop(0);
    }

    @Test
    void aHealthyExportStreamsTheRows() {
        ResponseEntity<String> export = get("/1/export?format=json");

        assertThat(export.getStatusCode().value()).isEqualTo(200);
        assertThat(export.getBody()).startsWith("{\"customer\":[").contains("\"id\":3").doesNotContain("\"id\":4");
    }

    @Test
    void aSlowButHealthyManagerIsWaitedForUpToTheConfiguredTenSeconds() {
        // Two seconds: the default TimeLimiter (1 s), which ran before the breaker was named, sent this to the fallback.
        ResponseEntity<String> export = get("/2/export?format=json");

        assertThat(export.getStatusCode().value()).isEqualTo(200);
        assertThat(export.getBody()).contains("\"customer\"");
    }

    @Test
    void anUnknownProjectIsANotFoundAndDoesNotCountAsAnOutage() {
        for (int i = 0; i < 6; i++) {
            ResponseEntity<String> export = get("/404/export?format=json");
            assertThat(export.getStatusCode().value()).isEqualTo(404);
            assertThat(export.getBody()).contains("Project not found with id: 404");
        }

        assertThat(breaker().getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void anOutageGivesThePartialFallbackWithoutAStackTrace() {
        ResponseEntity<String> export = get("/500/export?format=json");
        ResponseEntity<String> preview = get("/500/preview");

        assertThat(export.getStatusCode().value()).isEqualTo(503);
        assertThat(export.getBody()).contains("\"status\":\"PARTIAL\"").contains("\"data\":[]")
                .doesNotContain("Exception").doesNotContain("trace");
        assertThat(preview.getStatusCode().value()).isEqualTo(200);
        assertThat(preview.getBody()).contains("\"status\":\"PARTIAL\"");
    }

    @Test
    void theCircuitOpensWithTheSettingsFromConfigRepo() {
        for (int i = 0; i < 5; i++) {
            get("/500/export?format=json");
        }

        assertThat(circuitBreakers.getAllCircuitBreakers()).extracting(CircuitBreaker::getName)
                .containsExactly("datasetManagerClient");
        assertThat(breaker().getCircuitBreakerConfig().getMinimumNumberOfCalls()).isEqualTo(5);
        assertThat(breaker().getState()).isEqualTo(CircuitBreaker.State.OPEN);
        // Open: the manager is no longer called, even for a project that exists.
        assertThat(get("/1/export?format=json").getStatusCode().value()).isEqualTo(503);
    }

    @Test
    void anUnknownFormatIsABadRequest() {
        ResponseEntity<String> export = get("/1/export?format=pdf");

        assertThat(export.getStatusCode().value()).isEqualTo(400);
        assertThat(export.getBody()).contains("Unsupported export format: pdf");
    }

    private ResponseEntity<String> get(String uri) {
        return generator.get().uri(uri).retrieve().toEntity(String.class);
    }

    private CircuitBreaker breaker() {
        return circuitBreakers.circuitBreaker("datasetManagerClient");
    }

    /** Project 1 exists, 2 exists but answers in 2 s, 404 does not exist, anything else is an outage. */
    private static HttpServer fakeManager() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.createContext("/api/projects/", exchange -> {
                String path = exchange.getRequestURI().getPath();
                int status = 200;
                String body;
                if (path.matches("/api/projects/[12]")) {
                    body = "{\"id\":1,\"name\":\"shop\"}";
                } else if (path.matches("/api/projects/[12]/entities")) {
                    if (path.contains("/2/")) {
                        sleep(2_000);
                    }
                    body = """
                            [{"id":1,"name":"customer","quantity":3,"projectId":1,
                              "attributes":[{"name":"email","type":{"typeClass":"StringType"}}],"subEntities":[]}]""";
                } else if (path.startsWith("/api/projects/404")) {
                    status = 404;
                    body = "{\"status\":404,\"error\":\"Not Found\",\"message\":\"Project not found with id: 404\"}";
                } else {
                    status = 500;
                    body = "{\"status\":500}";
                }
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(status, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
