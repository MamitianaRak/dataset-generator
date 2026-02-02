package com.example.microservice.generation.interfaces;

import com.example.microservice.model.entities.Attribute;

import java.util.List;

/**
 * Générateur de données centré sur un Attribute : génère une valeur ou une liste de valeurs pour cet attribut.
 */
public interface IDataGenerator {
    /** Génère une seule valeur pour l'attribut donné */
    Object generate(Attribute attribute);

    /** Génère plusieurs valeurs pour l'attribut donné */
    default List<Object> generate(Attribute attribute, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> generate(attribute))
                .toList();
    }
}
