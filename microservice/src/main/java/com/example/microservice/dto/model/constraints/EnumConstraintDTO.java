package com.example.microservice.dto.model.constraints;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class EnumConstraintDTO extends ConstraintDTO {
    private List<String> allowed;
}
