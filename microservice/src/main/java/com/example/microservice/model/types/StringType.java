package com.example.microservice.model.types;

import com.example.microservice.model.interfaces.IDataType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@DiscriminatorValue("STRING")
@NoArgsConstructor
@Getter
@Setter
public class StringType extends BaseDataType implements IDataType {
    @Override
    public Object generateRandomValue() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public boolean validate(Object value) {
        return value instanceof String;
    }
}

