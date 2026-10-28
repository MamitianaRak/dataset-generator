package com.example.generator_service.feign;

import feign.FeignException;
import java.util.function.Predicate;

/**
 * Exceptions the circuit breaker must ignore: a 4xx from dataset-manager (an unknown project) means the service
 * answered. Counting it as a failure would let a few bad requests open the circuit for every user. The cause chain
 * is walked because the TimeLimiter wraps the Feign error in an ExecutionException.
 * Referenced from config-repo/generator.properties ({@code ignoreExceptionPredicate}).
 */
public class ClientErrorPredicate implements Predicate<Throwable> {

    @Override
    public boolean test(Throwable throwable) {
        for (Throwable t = throwable; t != null; t = t.getCause()) {
            if (t instanceof FeignException.FeignClientException) {
                return true;
            }
        }
        return false;
    }
}
