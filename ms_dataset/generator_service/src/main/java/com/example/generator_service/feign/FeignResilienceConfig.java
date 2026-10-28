package com.example.generator_service.feign;

import java.beans.Introspector;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class FeignResilienceConfig {

    /**
     * One circuit breaker per Feign client, named after the client interface: {@code datasetManagerClient}, the
     * name config-repo/generator.properties configures. By default Spring Cloud OpenFeign names one per method
     * ({@code DatasetManagerClientgetProjectEntitiesLong}), so the configured thresholds and the 10 s TimeLimiter
     * were never applied: the defaults ran instead, including a 1 s timeout that sent slow but healthy calls to
     * the fallback.
     */
    @Bean
    CircuitBreakerNameResolver circuitBreakerNameResolver() {
        return (feignClientName, target, method) -> Introspector.decapitalize(target.type().getSimpleName());
    }
}
