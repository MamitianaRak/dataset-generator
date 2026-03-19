package com.example.microservice.model.types;

import com.example.microservice.model.interfaces.IDataType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@DiscriminatorValue("BOOLEAN")
@NoArgsConstructor
@Getter
@Setter
public class BooleanType extends BaseDataType implements IDataType {
    @Override
    public Object generateRandomValue() {
        return Math.random() > 0.5;
    }

    @Override
    public Object generateRandomInRange(double min, double max) {
        return generateRandomValue();
    }

    @Override
    public boolean validate(Object value) {
        return value instanceof Boolean;
    }
}

