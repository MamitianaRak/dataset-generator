package com.example.microservice.model.types;

import com.example.microservice.model.interfaces.IDataType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Random;

@Entity
@DiscriminatorValue("INTEGER")
@NoArgsConstructor
@Getter
@Setter
public class IntegerType extends BaseDataType implements IDataType {
    @Transient
    private final Random rnd = new Random();

    @Override
    public Object generateRandomValue() {
        return rnd.nextInt(1000);
    }

    @Override
    public Object generateRandomInRange(double min, double max) {
        return (int) (min + rnd.nextInt((int) (max - min + 1)));
    }

    @Override
    public boolean validate(Object value) {
        return value instanceof Integer;
    }
}

