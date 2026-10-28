package com.example.microservice.model.schema;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

/** Rule a generated value must follow. A value object, stored as JSON with its attribute. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ValueConstraint.Range.class, name = "range"),
        @JsonSubTypes.Type(value = ValueConstraint.AllowedValues.class, name = "allowed"),
})
public sealed interface ValueConstraint {

    record Range(double min, double max) implements ValueConstraint {
        public Range {
            if (min > max) {
                throw new IllegalArgumentException("min must be less than or equal to max");
            }
        }
    }

    record AllowedValues(List<String> values) implements ValueConstraint {
        public AllowedValues {
            values = values == null ? List.of() : List.copyOf(values);
        }
    }
}
