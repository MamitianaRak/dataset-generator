package com.example.microservice.dto.generation;

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
}
