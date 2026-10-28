package com.example.microservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/**
 * The API through real HTTP and JSON, as the front end and Bruno call it: deserialisation is part of the contract
 * (the service-level tests bypass it, which is how a rejected body went unnoticed).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.cloud.config.enabled=false", "eureka.client.enabled=false",
                // The /api prefix comes from config-repo, which tests do not load.
                "server.servlet.context-path=/api"})
class EntityJsonApiTest {

    @LocalServerPort
    private int port;

    private RestClient api;

    @BeforeEach
    void setUp() {
        api = RestClient.builder().baseUrl("http://localhost:" + port + "/api")
                .defaultStatusHandler(status -> true, (request, response) -> { })
                .build();
    }

    @Test
    void anAttributeWithoutTheOptionalQuantityIsAccepted() {
        long projectId = createProject("json-optional-quantity");

        // What the front end sends: no "quantity" on attributes. Jackson 3 refused it by default.
        ResponseEntity<String> created = post("/projects/" + projectId + "/entities", """
                {"name": "customer", "quantity": 20, "attributes": [
                  {"name": "email", "type": {"typeClass": "StringType"}},
                  {"name": "age", "type": {"typeClass": "IntegerType"},
                   "constraints": [{"constraintClass": "MinMaxConstraint", "min": 18, "max": 90}]}]}""");

        assertThat(created.getStatusCode().value()).isEqualTo(201);
        assertThat(created.getBody()).contains("\"name\":\"email\"").contains("\"min\":18.0");
    }

    @Test
    void aMissingEntityQuantityIsAValidationErrorNotAnUnreadableBody() {
        long projectId = createProject("json-missing-quantity");

        ResponseEntity<String> refused = post("/projects/" + projectId + "/entities", """
                {"name": "customer", "attributes": []}""");

        assertThat(refused.getStatusCode().value()).isEqualTo(400);
        assertThat(refused.getBody()).contains("Quantity must be at least 1");
    }

    @Test
    void anUnknownTypeIsReportedAsSuch() {
        long projectId = createProject("json-unknown-type");

        ResponseEntity<String> refused = post("/projects/" + projectId + "/entities", """
                {"name": "t", "quantity": 1, "attributes": [{"name": "x", "type": {"typeClass": "UuidType"}}]}""");

        assertThat(refused.getStatusCode().value()).isEqualTo(400);
        assertThat(refused.getBody()).contains("Unknown type");
    }

    private long createProject(String name) {
        String body = post("/projects", "{\"name\": \"" + name + "\"}").getBody();
        return Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private ResponseEntity<String> post(String uri, String json) {
        return api.post().uri(uri).contentType(MediaType.APPLICATION_JSON).body(json).retrieve().toEntity(String.class);
    }
}
