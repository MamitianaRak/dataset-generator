package com.example.microservice.dto.model.types;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class IntegerTypeDTO extends DataTypeDTO {
    // Pas de champs supplémentaires, juste le type
}
