package com.example.microservice.dto.model.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * DTO pour les types de données.
 * Utilise la polymorphie JSON pour supporter différents types.
 */
@Data
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "typeClass"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = IntegerTypeDTO.class, name = "IntegerType"),
    @JsonSubTypes.Type(value = FloatTypeDTO.class, name = "FloatType"),
    @JsonSubTypes.Type(value = StringTypeDTO.class, name = "StringType"),
    @JsonSubTypes.Type(value = BooleanTypeDTO.class, name = "BooleanType"),
    @JsonSubTypes.Type(value = DateTypeDTO.class, name = "DateType"),
    @JsonSubTypes.Type(value = EnumTypeDTO.class, name = "EnumType")
})
public abstract class DataTypeDTO {
    // Classe de base pour tous les types
}
