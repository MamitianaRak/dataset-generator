package com.example.microservice.distribution;

import com.example.microservice.distribution.interfaces.IDistribution;
import com.example.microservice.model.interfaces.IConstraint;
import com.example.microservice.model.interfaces.IDataType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Entity
@DiscriminatorValue("CUSTOM")
@NoArgsConstructor
@Getter
@Setter
public class CustomDistribution extends BaseDistribution implements IDistribution {
    @Transient
    private List<Object> values = new ArrayList<>();

    @Transient
    private List<Double> cumulative = new ArrayList<>();

    @Transient
    private final Random rng = new Random();

    @Column(name = "probabilities_json", columnDefinition = "TEXT")
    private String probabilitiesJson;

    public CustomDistribution(Map<Object, Double> probabilities) {
        double sum = 0.0;
        for (Map.Entry<Object, Double> e : probabilities.entrySet()) {
            values.add(e.getKey());
            sum += e.getValue();
            cumulative.add(sum);
        }
        if (sum > 0) {
            for (int i = 0; i < cumulative.size(); i++) {
                cumulative.set(i, cumulative.get(i) / sum);
            }
        }
    }

    @Override
    public Object nextValue(IDataType dataType, IConstraint constraint) {
        if (values.isEmpty()) return null;
        double r = rng.nextDouble();
        for (int i = 0; i < cumulative.size(); i++) {
            if (r <= cumulative.get(i)) return values.get(i);
        }
        return values.get(values.size() - 1);
    }
}

