package com.example.microservice.model.types;

import com.example.microservice.model.interfaces.IDataType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Entity
@DiscriminatorValue("ENUM")
@NoArgsConstructor
@Getter
@Setter
public class EnumType extends BaseDataType implements IDataType {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "enum_type_values", joinColumns = @JoinColumn(name = "enum_type_id"))
    @Column(name = "enum_value")
    private List<String> values = new ArrayList<>();

    @Transient
    private final Random rnd = new Random();

    public EnumType(List<String> values) {
        this.values = values;
    }

    @Override
    public Object generateRandomValue() {
        if (values == null || values.isEmpty()) return null;
        return values.get(rnd.nextInt(values.size()));
    }

    @Override
    public Object generateRandomInRange(double min, double max) {
        return generateRandomValue();
    }

    @Override
    public boolean validate(Object value) {
        if (value == null) return false;
        return values.contains(value.toString());
    }
}
