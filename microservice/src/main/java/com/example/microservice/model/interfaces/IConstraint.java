package com.example.microservice.model.interfaces;

/**
 * Interface pour une contrainte sur une valeur.
 */
public interface IConstraint {
    boolean validate(Object value);
}
