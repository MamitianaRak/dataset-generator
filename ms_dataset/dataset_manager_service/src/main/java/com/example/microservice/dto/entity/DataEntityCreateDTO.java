package com.example.microservice.dto.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour créer une DataEntity dans un projet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataEntityCreateDTO {
    /** Rows per table. Exports stream, so this bounds the export's duration and size, not the server's memory. */
    public static final int MAX_ROWS = 1_000_000;

    @NotBlank(message = "Entity name is required")
    private String name;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = MAX_ROWS, message = "Quantity must be at most 1000000")
    private int quantity;

    @Valid
    private List<AttributeDTO> attributes;

    @Valid
    private List<DataEntityCreateDTO> subEntities;
}
