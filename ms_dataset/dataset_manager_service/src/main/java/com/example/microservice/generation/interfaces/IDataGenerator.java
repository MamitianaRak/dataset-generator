package com.example.microservice.generation.interfaces;

import com.example.microservice.model.entities.Attribute;

/**
 * Générateur de données centré sur un Attribute : génère une valeur pour cet attribut.
 */
public interface IDataGenerator {
    /** Génère une seule valeur pour l'attribut donné */
    Object generate(Attribute attribute);
}
