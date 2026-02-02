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
    public boolean validate(Object value) {
        return value instanceof Boolean;
    }
}

