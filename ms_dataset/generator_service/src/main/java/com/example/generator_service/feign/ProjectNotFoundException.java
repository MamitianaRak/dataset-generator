package com.example.generator_service.feign;

/** dataset-manager answered, and the project does not exist: a client error, not an outage. */
public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(Long projectId) {
        super("Project not found with id: " + projectId);
    }
}
