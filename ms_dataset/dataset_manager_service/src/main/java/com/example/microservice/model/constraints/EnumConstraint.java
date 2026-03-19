package com.example.microservice.model.constraints;

import com.example.microservice.model.interfaces.IConstraint;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("ENUM")
@NoArgsConstructor
@Getter
@Setter
public class EnumConstraint extends BaseConstraint implements IConstraint {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "enum_constraint_values", joinColumns = @JoinColumn(name = "enum_constraint_id"))
    @Column(name = "allowed_value")
    private List<String> allowed = new ArrayList<>();

    public EnumConstraint(List<String> allowed) {
        this.allowed = allowed;
    }

    @Override
    public boolean validate(Object value) {
        if (value == null) return false;
        return allowed.contains(value.toString());
    }
}

