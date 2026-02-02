package com.example.microservice.generation;

import com.example.microservice.generation.interfaces.IDataGenerator;
import com.example.microservice.model.entities.Attribute;
import jakarta.persistence.*;
import lombok.*;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Générateur séquentiel simple : renvoie un nombre incrémental pour les attributs de type numérique ou dont le nom contient "id".
 */
@Entity
@DiscriminatorValue("SEQUENTIAL")
@NoArgsConstructor
@Getter
@Setter
public class SequentialDataGenerator extends BaseDataGenerator implements IDataGenerator {
    private long startValue = 1;

    @Transient
    private AtomicLong counter;

    public SequentialDataGenerator(long startValue) {
        this.startValue = startValue;
        this.counter = new AtomicLong(startValue);
    }

    @PostLoad
    private void initCounter() {
        if (counter == null) {
            counter = new AtomicLong(startValue);
        }
    }

    @Override
    public Object generate(Attribute attribute) {
        if (attribute == null) throw new IllegalArgumentException("attribute == null");
        if (counter == null) {
            counter = new AtomicLong(startValue);
        }
        String name = attribute.getName();
        if (name != null && ("id".equalsIgnoreCase(name) || name.toLowerCase().endsWith("id"))) {
            long value = counter.getAndIncrement();
            if (attribute.getType() != null) {
                String typeName = attribute.getType().getClass().getSimpleName();
                if ("IntegerType".equals(typeName)) {
                    return (int) value;
                }
            }
            return value;
        }
        if (attribute.getType() != null) {
            return attribute.getType().generateRandomValue();
        }
        return null;
    }
}

