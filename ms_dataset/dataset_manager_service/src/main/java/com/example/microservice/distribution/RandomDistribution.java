package com.example.microservice.distribution;

import com.example.microservice.distribution.interfaces.IDistribution;
import com.example.microservice.model.interfaces.IConstraint;
import com.example.microservice.model.interfaces.IDataType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Random;

@Entity
@DiscriminatorValue("RANDOM")
@NoArgsConstructor
@Getter
@Setter
public class RandomDistribution extends BaseDistribution implements IDistribution {
    @Transient
    private final Random rng = new Random();

    @Override
    public Object nextValue(IDataType dataType, IConstraint constraint) {
        return dataType != null ? dataType.generateRandomValue() : null;
    }
}

