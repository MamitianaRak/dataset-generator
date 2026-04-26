package com.example.generator_service.dto.model.constraints;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MinMaxConstraintDTO extends ConstraintDTO {
    private double min;
    private double max;

    @JsonIgnore
    @AssertTrue(message = "min must be less than or equal to max")
    public boolean isValidMinMax() {
        return min <= max;
    }
}
