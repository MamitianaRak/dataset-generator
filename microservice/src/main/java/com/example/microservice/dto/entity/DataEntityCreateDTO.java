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
 * DTO pour créer une DataEntity dans un projet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataEntityCreateDTO {
    private String name;
    private int quantity;
    private List<AttributeDTO> attributes;
    private List<DataEntityCreateDTO> subEntities;
}
