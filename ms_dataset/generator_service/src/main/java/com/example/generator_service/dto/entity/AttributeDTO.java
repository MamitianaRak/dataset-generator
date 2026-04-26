package com.example.generator_service.dto.entity;

import com.example.generator_service.dto.model.constraints.ConstraintDTO;
import com.example.generator_service.dto.model.generators.GeneratorDTO;
import com.example.generator_service.dto.model.types.DataTypeDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour un attribut d'une entité.
 * Contient le type, les contraintes, le générateur, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDTO {
    private Long id;

    @NotBlank(message = "Attribute name is required")
    private String name;

    @NotNull(message = "Attribute type is required")
    @Valid
    private DataTypeDTO type;

    @Valid
    private List<ConstraintDTO> constraints;

    private int quantity;

    @Valid
    private GeneratorDTO generator;
}
