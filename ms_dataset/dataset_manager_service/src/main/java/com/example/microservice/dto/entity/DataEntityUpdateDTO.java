package com.example.microservice.dto.entity;

import com.example.microservice.dto.entity.AttributeDTO;

import lombok.AllArgsConstructor;
import com.example.microservice.dto.entity.AttributeDTO;

import lombok.Builder;
import com.example.microservice.dto.entity.AttributeDTO;

import lombok.Data;
import com.example.microservice.dto.entity.AttributeDTO;

import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour mettre à jour une DataEntity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataEntityUpdateDTO {
    private String name;
    private int quantity;
    private List<AttributeDTO> attributes;
}
