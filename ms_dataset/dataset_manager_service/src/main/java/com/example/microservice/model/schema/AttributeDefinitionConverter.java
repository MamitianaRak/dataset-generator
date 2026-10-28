package com.example.microservice.model.schema;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.databind.json.JsonMapper;

/**
 * Maps {@link AttributeDefinition} to its JSON text. Written explicitly rather than left to Hibernate's own JSON
 * support, which picks whichever Jackson version happens to be on the classpath (both are, through Spring Cloud).
 */
@Converter
public class AttributeDefinitionConverter implements AttributeConverter<AttributeDefinition, String> {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Override
    public String convertToDatabaseColumn(AttributeDefinition definition) {
        return definition == null ? null : JSON.writeValueAsString(definition);
    }

    @Override
    public AttributeDefinition convertToEntityAttribute(String json) {
        return json == null ? null : JSON.readValue(json, AttributeDefinition.class);
    }
}
