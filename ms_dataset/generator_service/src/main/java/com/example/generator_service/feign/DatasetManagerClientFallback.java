package com.example.generator_service.feign;

import com.example.generator_service.dto.entity.DataEntityResponseDTO;
import com.example.generator_service.dto.project.ProjectResponseDTO;
import feign.FeignException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback de DatasetManagerClient. Il reçoit la cause de l'échec, ce qu'un simple fallback ne permet pas :
 * un projet inexistant (404) reste une erreur client, seule une vraie panne déclenche la réponse dégradée.
 * Il ne fabrique jamais de données vides, qui feraient passer une panne pour un export réussi.
 */
@Component
@Slf4j
public class DatasetManagerClientFallback implements FallbackFactory<DatasetManagerClient> {

    @Override
    public DatasetManagerClient create(Throwable cause) {
        return new DatasetManagerClient() {
            @Override
            public ProjectResponseDTO getProject(Long projectId) {
                throw failure(projectId, cause);
            }

            @Override
            public List<DataEntityResponseDTO> getProjectEntities(Long projectId) {
                throw failure(projectId, cause);
            }
        };
    }

    private static RuntimeException failure(Long projectId, Throwable cause) {
        // The TimeLimiter runs the call on another thread: the Feign error arrives wrapped (ExecutionException).
        for (Throwable t = cause; t != null; t = t.getCause()) {
            if (t instanceof FeignException.NotFound) {
                return new ProjectNotFoundException(projectId);
            }
        }
        log.warn("Fallback: dataset-manager indisponible pour le projet {} ({})", projectId, cause.toString());
        return new DatasetManagerUnavailableException(cause);
    }
}
