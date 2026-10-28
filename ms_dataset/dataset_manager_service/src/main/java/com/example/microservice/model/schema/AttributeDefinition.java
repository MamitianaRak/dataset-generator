package com.example.microservice.model.schema;

import java.util.List;
import java.util.Objects;

/** What an attribute generates: its type and constraints, stored together in one JSON column. */
public record AttributeDefinition(ValueType type, List<ValueConstraint> constraints) {

    public AttributeDefinition {
        Objects.requireNonNull(type, "type");
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
    }
}
