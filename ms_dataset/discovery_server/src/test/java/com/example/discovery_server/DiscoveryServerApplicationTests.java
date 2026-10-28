package com.example.discovery_server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Without the config server (its client is fail-fast); the server must not try to register with itself.
@SpringBootTest(properties = {"spring.cloud.config.enabled=false",
        "eureka.client.register-with-eureka=false", "eureka.client.fetch-registry=false"})
class DiscoveryServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
