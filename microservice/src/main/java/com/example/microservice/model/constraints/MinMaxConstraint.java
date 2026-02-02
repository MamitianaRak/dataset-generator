package com.example.microservice.model.constraints;

import com.example.microservice.model.interfaces.IConstraint;
import jakarta.persistence.*;
import lombok.*;

@Entity
@DiscriminatorValue("MIN_MAX")
@NoArgsConstructor
@Getter
@Setter
public class MinMaxConstraint extends BaseConstraint implements IConstraint {
    private double min;
    private double max;

    public MinMaxConstraint(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean validate(Object value) {
        if (!(value instanceof Number)) return false;
        double v = ((Number) value).doubleValue();
        return v >= min && v <= max;
    }
}

