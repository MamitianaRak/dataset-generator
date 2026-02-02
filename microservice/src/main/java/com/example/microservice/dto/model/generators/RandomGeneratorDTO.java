package com.example.microservice.dto.model.generators;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RandomGeneratorDTO extends GeneratorDTO {
    // Pas de configuration supplémentaire pour RandomDataGenerator
}
