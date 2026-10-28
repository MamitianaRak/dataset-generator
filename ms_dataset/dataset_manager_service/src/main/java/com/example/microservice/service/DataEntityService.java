package com.example.microservice.service;

import com.example.microservice.dto.entity.DataEntityCreateDTO;
import com.example.microservice.dto.entity.DataEntityResponseDTO;
import com.example.microservice.dto.entity.DataEntityUpdateDTO;

import java.util.List;

/**
 * Service pour gérer les DataEntity.
 */
public interface DataEntityService {
    /**
     * Crée une nouvelle entité dans un projet.
     */
    DataEntityResponseDTO createEntity(Long projectId, DataEntityCreateDTO dto);

    /**
     * Ajoute une sous-entité à une entité existante (dans le même projet).
     */
    DataEntityResponseDTO createSubEntity(Long parentId, DataEntityCreateDTO dto);

    /**
     * Liste toutes les entités d'un projet.
     */
    List<DataEntityResponseDTO> findEntitiesByProject(Long projectId);

    /**
     * Récupère une entité par son ID.
     */
    DataEntityResponseDTO findById(Long id);

    /**
     * Met à jour une entité.
     */
    DataEntityResponseDTO updateEntity(Long id, DataEntityUpdateDTO dto);

    /**
     * Supprime une entité.
     */
    void deleteEntity(Long id);
}
