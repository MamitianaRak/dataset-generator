package com.example.generator_service.dto.generation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO représentant les données générées pour une entité.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityDataDTO {
    private String entityName;
    private List<RecordDTO> records;
}
