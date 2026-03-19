package com.example.microservice.model.types;

import com.example.microservice.model.interfaces.IDataType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@DiscriminatorValue("DATE")
@NoArgsConstructor
@Getter
@Setter
public class DateType extends BaseDataType implements IDataType {
    @Override
    public Object generateRandomValue() {
        long startEpoch = LocalDate.of(1970, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endEpoch = Instant.now().toEpochMilli();
        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
        return new Date(randomEpoch);
    }

    @Override
    public Object generateRandomInRange(double min, double max) {
        long randomEpoch = ThreadLocalRandom.current().nextLong((long) min, (long) max);
        return new Date(randomEpoch);
    }

    @Override
    public boolean validate(Object value) {
        return value instanceof Date;
    }
}

