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
 * DTO pour la réponse d'une DataEntity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataEntityResponseDTO {
    private Long id;
    private String name;
    private int quantity;
    private Long projectId;
    private List<AttributeDTO> attributes;
    private List<DataEntityResponseDTO> subEntities;
}
