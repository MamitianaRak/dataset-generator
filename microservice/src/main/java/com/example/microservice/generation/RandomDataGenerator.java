package com.example.microservice.generation;

import com.example.microservice.generation.interfaces.IDataGenerator;
import com.example.microservice.model.constraints.MinMaxConstraint;
import com.example.microservice.model.entities.Attribute;
import com.example.microservice.model.interfaces.IConstraint;
import com.example.microservice.model.interfaces.IDataType;
import com.example.microservice.model.types.FloatType;
import com.example.microservice.model.types.IntegerType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Random;

/**
 * Générateur qui produit une valeur pour un Attribute en respectant son type et ses contraintes.
 * Si une contrainte MinMaxConstraint est présente, génère directement dans cette plage.
 */
@Entity
@DiscriminatorValue("RANDOM")
@NoArgsConstructor
@Getter
@Setter
public class RandomDataGenerator extends BaseDataGenerator implements IDataGenerator {
    @Transient
    private final Random rnd = new Random();

    @Override
    public Object generate(Attribute attribute) {
        if (attribute == null) throw new IllegalArgumentException("attribute == null");
        IDataType type = attribute.getType();

        // Recherche une contrainte MinMaxConstraint
        MinMaxConstraint minMaxConstraint = findMinMaxConstraint(attribute);

        // Génération intelligente basée sur le type et les contraintes
        if (minMaxConstraint != null && type instanceof IntegerType) {
            return (int) (minMaxConstraint.getMin() + rnd.nextInt((int) (minMaxConstraint.getMax() - minMaxConstraint.getMin() + 1)));
        } else if (minMaxConstraint != null && type instanceof FloatType) {
            return minMaxConstraint.getMin() + rnd.nextDouble() * (minMaxConstraint.getMax() - minMaxConstraint.getMin());
        }

        // Génération par défaut via le type
        Object value = (type != null) ? type.generateRandomValue() : null;

        // Validation avec retry limité pour autres contraintes
        if (attribute.getConstraints() != null && !attribute.getConstraints().isEmpty()) {
            int attempts = 0;
            while (!attribute.validate(value) && attempts < 10) {
                value = (type != null) ? type.generateRandomValue() : null;
                attempts++;
            }
        }
        return value;
    }

    private MinMaxConstraint findMinMaxConstraint(Attribute attribute) {
        if (attribute.getConstraints() == null) return null;
        for (IConstraint constraint : attribute.getConstraints()) {
            if (constraint instanceof MinMaxConstraint) {
                return (MinMaxConstraint) constraint;
            }
        }
        return null;
    }
}

