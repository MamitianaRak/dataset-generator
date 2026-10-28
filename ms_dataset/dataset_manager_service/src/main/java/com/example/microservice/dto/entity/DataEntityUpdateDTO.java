package com.example.microservice.dto.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour mettre à jour une DataEntity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataEntityUpdateDTO {
    private String name;

    @Min(value = 0, message = "Quantity must not be negative")
    @Max(value = DataEntityCreateDTO.MAX_ROWS, message = "Quantity must be at most 1000000")
    private int quantity;

    @Valid
    private List<AttributeDTO> attributes;
}
