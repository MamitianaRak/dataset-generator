package com.example.microservice.dto.model.generators;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SequentialGeneratorDTO extends GeneratorDTO {
    private long startValue = 1;
}
