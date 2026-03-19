package com.example.microservice.model.types;

import com.example.microservice.model.interfaces.IDataType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Random;

@Entity
@DiscriminatorValue("FLOAT")
@NoArgsConstructor
@Getter
@Setter
public class FloatType extends BaseDataType implements IDataType {
    @Transient
    private final Random rnd = new Random();

    @Override
    public Object generateRandomValue() {
        return rnd.nextDouble() * 1000.0;
    }

    @Override
    public Object generateRandomInRange(double min, double max) {
        return min + rnd.nextDouble() * (max - min);
    }

    @Override
    public boolean validate(Object value) {
        return value instanceof Number;
    }
}

