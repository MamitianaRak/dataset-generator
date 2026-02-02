package com.example.microservice.dto.model.generators;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * DTO pour les générateurs de données.
 */
@Data
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "generatorClass"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = RandomGeneratorDTO.class, name = "RandomGenerator"),
    @JsonSubTypes.Type(value = SequentialGeneratorDTO.class, name = "SequentialGenerator"),
    @JsonSubTypes.Type(value = EmailGeneratorDTO.class, name = "EmailGenerator")
})
public abstract class GeneratorDTO {
    // Classe de base pour tous les générateurs
}
