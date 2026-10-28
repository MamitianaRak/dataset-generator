package com.example.generator_service.feign;

/** dataset-manager is down, too slow, or the circuit breaker is open: the generator answers with its fallback. */
public class DatasetManagerUnavailableException extends RuntimeException {

    public DatasetManagerUnavailableException(Throwable cause) {
        super("Le service dataset-manager est indisponible. Veuillez réessayer plus tard.", cause);
    }
}
