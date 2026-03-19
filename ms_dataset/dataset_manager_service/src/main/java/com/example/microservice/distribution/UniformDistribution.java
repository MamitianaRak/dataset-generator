package com.example.microservice.distribution;

import com.example.microservice.distribution.interfaces.IDistribution;
import com.example.microservice.model.interfaces.IConstraint;
import com.example.microservice.model.interfaces.IDataType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@DiscriminatorValue("UNIFORM")
@NoArgsConstructor
@Getter
@Setter
public class UniformDistribution extends BaseDistribution implements IDistribution {
    @Override
    public Object nextValue(IDataType dataType, IConstraint constraint) {
        return dataType != null ? dataType.generateRandomValue() : null;
    }
}

