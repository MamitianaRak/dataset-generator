package com.example.generator_service.controller;

import com.example.generator_service.feign.DatasetManagerUnavailableException;
import com.example.generator_service.feign.ProjectNotFoundException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Errors as JSON with the right status, never a stack trace (the brief's validation scenario forbids one). */
@RestControllerAdvice
public class GeneratorExceptionHandler {

    /**
     * The fallback the brief asks for, {@code {"status": "PARTIAL", "data": []}} with a message, sent as 503 so
     * that a client, or a script downloading an export, cannot take an outage for an empty dataset.
     */
    @ExceptionHandler(DatasetManagerUnavailableException.class)
    public ResponseEntity<Map<String, Object>> unavailable(DatasetManagerUnavailableException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "PARTIAL");
        body.put("message", e.getMessage());
        body.put("data", List.of());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(ProjectNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** An unknown export format, for instance. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    private static ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
