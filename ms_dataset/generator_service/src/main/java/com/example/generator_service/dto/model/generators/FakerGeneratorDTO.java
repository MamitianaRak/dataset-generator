package com.example.generator_service.dto.model.generators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO pour FakerDataGenerator.
 * Génère des données réalistes via la bibliothèque Datafaker.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class FakerGeneratorDTO extends GeneratorDTO {
    /** Locale utilisée par Faker (ex: "fr", "en"). Par défaut "fr". */
    private String locale = "fr";
}

