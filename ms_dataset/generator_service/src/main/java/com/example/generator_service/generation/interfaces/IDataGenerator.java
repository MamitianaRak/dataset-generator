package com.example.generator_service.generation.interfaces;

import com.example.generator_service.dto.entity.AttributeDTO;

/**
 * Générateur de données stateless pour le generator-service.
 * Génère des valeurs à partir d'AttributeDTO uniquement.
 */
public interface IDataGenerator {
    /** Génère une seule valeur pour l'attribut donné (DTO) */
    Object generateFromDTO(AttributeDTO attribute);
}
