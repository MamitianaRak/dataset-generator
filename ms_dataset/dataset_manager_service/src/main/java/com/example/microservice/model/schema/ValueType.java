package com.example.microservice.model.schema;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;

/**
 * Type of a generated column. A value object owned by one attribute, stored as JSON with it: it used to be an
 * entity in its own table, which cost a join (and a cascade) per attribute for no shared identity.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ValueType.IntegerValue.class, name = "integer"),
        @JsonSubTypes.Type(value = ValueType.FloatValue.class, name = "float"),
        @JsonSubTypes.Type(value = ValueType.StringValue.class, name = "string"),
        @JsonSubTypes.Type(value = ValueType.BooleanValue.class, name = "boolean"),
        @JsonSubTypes.Type(value = ValueType.DateValue.class, name = "date"),
        @JsonSubTypes.Type(value = ValueType.EnumValue.class, name = "enum"),
})
public sealed interface ValueType {

    record IntegerValue() implements ValueType {
    }

    record FloatValue() implements ValueType {
    }

    record StringValue() implements ValueType {
    }

    record BooleanValue() implements ValueType {
    }

    record DateValue() implements ValueType {
    }

    record EnumValue(List<String> values) implements ValueType {
        public EnumValue {
            values = values == null ? List.of() : List.copyOf(values);
        }
    }
}
