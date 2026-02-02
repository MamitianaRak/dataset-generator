package com.example.microservice.dto.model.constraints;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MinMaxConstraintDTO extends ConstraintDTO {
    private double min;
    private double max;
}
