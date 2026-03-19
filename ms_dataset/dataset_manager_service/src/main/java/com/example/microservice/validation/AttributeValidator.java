package com.example.microservice.validation;

import com.example.microservice.model.constraints.BaseConstraint;
import com.example.microservice.model.entities.Attribute;
import com.example.microservice.model.interfaces.IConstraint;

/**
 * Classe utilitaire pour la validation des valeurs d'un attribut.
 * Sépare la logique de validation de l'entité JPA (SRP).
 */
public class AttributeValidator {

    private AttributeValidator() {}

    public static boolean validate(Attribute attribute, Object value) {
        if (attribute.getType() != null && !attribute.getType().validate(value)) {
            return false;
        }
        for (IConstraint c : attribute.getConstraints()) {
            if (!c.validate(value)) {
                return false;
            }
        }
        return true;
    }
}
