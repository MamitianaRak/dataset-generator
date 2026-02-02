package com.example.microservice.dto.model.generators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO pour EmailDataGenerator.
 * Génère des emails aléatoires au format prenom.nom@domaine.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EmailGeneratorDTO extends GeneratorDTO {
    // Pas de configuration supplémentaire nécessaire
    // Les emails sont générés aléatoirement
}
