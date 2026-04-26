package com.example.generator_service.dto.model.constraints;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * DTO pour les contraintes.
 * Utilise la polymorphie JSON pour supporter différentes contraintes.
 */
@Data
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "constraintClass"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = MinMaxConstraintDTO.class, name = "MinMaxConstraint"),
    @JsonSubTypes.Type(value = EnumConstraintDTO.class, name = "EnumConstraint")
})
public abstract class ConstraintDTO {
    // Classe de base pour toutes les contraintes
}
