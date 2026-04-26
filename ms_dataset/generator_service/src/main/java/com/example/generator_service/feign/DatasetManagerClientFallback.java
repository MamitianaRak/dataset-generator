package com.example.generator_service.feign;

import com.example.generator_service.dto.entity.DataEntityResponseDTO;
import com.example.generator_service.dto.project.ProjectResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fallback pour DatasetManagerClient.
 * Retourne des valeurs par défaut lorsque dataset-manager est indisponible.
 */
@Component
@Slf4j
public class DatasetManagerClientFallback implements DatasetManagerClient {

    @Override
    public ProjectResponseDTO getProject(Long projectId) {
        log.warn("Fallback: dataset-manager indisponible pour getProject({})", projectId);
        return null;
    }

    @Override
    public List<DataEntityResponseDTO> getProjectEntities(Long projectId) {
        log.warn("Fallback: dataset-manager indisponible pour getProjectEntities({})", projectId);
        return List.of();
    }
}
