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
    public Object generateRandomInRange(double min, double max) {
        int length = Math.max(1, (int) max);
        String full = UUID.randomUUID().toString().replace("-", "");
        return full.substring(0, Math.min(length, full.length()));
    }

    @Override
    public boolean validate(Object value) {
        return value instanceof String;
    }
}

