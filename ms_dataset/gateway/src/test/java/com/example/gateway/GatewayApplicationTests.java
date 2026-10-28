package com.example.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Without the config server and Eureka: the config client is fail-fast and would stop the context.
@SpringBootTest(properties = {"spring.cloud.config.enabled=false", "eureka.client.enabled=false"})
class GatewayApplicationTests {

    @Test
    void contextLoads() {
    }

}
