package com.example.microservice.service;

import com.example.microservice.dataset.Dataset;
import com.example.microservice.dto.generation.PreviewResponseDTO;

/**
 * Service dédié à la génération de données pour les projets.
 * Séparé du CRUD ProjectService (SRP).
 */
public interface DataGenerationService {
    PreviewResponseDTO generatePreview(Long projectId);
    Dataset generateDataset(Long projectId);
}
