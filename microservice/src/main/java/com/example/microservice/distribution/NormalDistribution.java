package com.example.microservice.distribution;

import com.example.microservice.distribution.interfaces.IDistribution;
import com.example.microservice.model.interfaces.IConstraint;
import com.example.microservice.model.interfaces.IDataType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Random;

@Entity
@DiscriminatorValue("NORMAL")
@NoArgsConstructor
@Getter
@Setter
public class NormalDistribution extends BaseDistribution implements IDistribution {
    @Transient
    private final Random rng = new Random();

    private double mean = 0.0;
    private double stddev = 1.0;

    public NormalDistribution(double mean, double stddev) {
        this.mean = mean;
        this.stddev = stddev;
    }

    @Override
    public Object nextValue(IDataType dataType, IConstraint constraint) {
        return mean + rng.nextGaussian() * stddev;
    }
}

