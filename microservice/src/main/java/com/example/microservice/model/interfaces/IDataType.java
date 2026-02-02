package com.example.microservice.model.interfaces;

/**
 * Interface représentant un type capable de générer et valider des valeurs.
 */
public interface IDataType {
    Object generateRandomValue();
    boolean validate(Object value);
}
