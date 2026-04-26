package com.example.generator_service.dto.generation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la réponse de prévisualisation des données générées.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewResponseDTO {
    private Long projectId;
    private String projectName;
    private List<EntityDataDTO> data;

    // Champs pour le fallback Resilience4J
    private String status;   // "OK" ou "PARTIAL"
    private String message;  // Message d'erreur si fallback
}
