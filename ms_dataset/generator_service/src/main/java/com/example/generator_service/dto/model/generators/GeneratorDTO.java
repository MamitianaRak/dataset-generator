package com.example.generator_service.dto.model.generators;

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
    @JsonSubTypes.Type(value = FakerGeneratorDTO.class, name = "FakerGenerator"),
    @JsonSubTypes.Type(value = SequentialGeneratorDTO.class, name = "SequentialGenerator")
})
public abstract class GeneratorDTO {
    // Classe de base pour tous les générateurs
}
