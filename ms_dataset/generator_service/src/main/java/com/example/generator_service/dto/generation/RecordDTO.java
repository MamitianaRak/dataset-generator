package com.example.generator_service.dto.generation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO représentant un enregistrement généré.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordDTO {
    private Map<String, Object> fields;
}
