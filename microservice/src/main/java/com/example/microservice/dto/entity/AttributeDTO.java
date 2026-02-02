package com.example.microservice.dto.entity;

import com.example.microservice.dto.model.constraints.ConstraintDTO;
import com.example.microservice.dto.model.generators.GeneratorDTO;
import com.example.microservice.dto.model.types.DataTypeDTO;
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
    private String name;
    private DataTypeDTO type;
    private List<ConstraintDTO> constraints;
    private int quantity;
    private GeneratorDTO generator;
}
