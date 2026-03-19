package com.example.microservice.dto.model.types;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class EnumTypeDTO extends DataTypeDTO {
    private List<String> values;
}
