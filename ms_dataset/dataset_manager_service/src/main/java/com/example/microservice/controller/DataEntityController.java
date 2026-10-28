package com.example.microservice.controller;

import com.example.microservice.dto.entity.DataEntityCreateDTO;
import com.example.microservice.dto.entity.DataEntityResponseDTO;
import com.example.microservice.dto.entity.DataEntityUpdateDTO;
import com.example.microservice.service.DataEntityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour gérer les DataEntity.
 * Implémente les endpoints de modélisation des données.
 */
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RestController
@RequiredArgsConstructor
public class DataEntityController {

    private final DataEntityService dataEntityService;

    /**
     * POST /api/projects/{projectId}/entities - Ajouter une entité dans un projet
     */
    @PostMapping("/projects/{projectId}/entities")
    @ResponseStatus(HttpStatus.CREATED)
    public DataEntityResponseDTO createEntity(
            @PathVariable("projectId") Long projectId,
            @Valid @RequestBody DataEntityCreateDTO dto) {
        return dataEntityService.createEntity(projectId, dto);
    }

    /**
     * POST /api/entities/{parentId}/sub-entities - Ajouter une sous-entité (table liée) à une entité existante
     */
    @PostMapping("/entities/{parentId}/sub-entities")
    @ResponseStatus(HttpStatus.CREATED)
    public DataEntityResponseDTO createSubEntity(
            @PathVariable("parentId") Long parentId,
            @Valid @RequestBody DataEntityCreateDTO dto) {
        return dataEntityService.createSubEntity(parentId, dto);
    }

    /**
     * GET /api/projects/{projectId}/entities - Lister toutes les entités dans un projet
     */
    @GetMapping("/projects/{projectId}/entities")
    public List<DataEntityResponseDTO> getEntitiesByProject(@PathVariable("projectId") Long projectId) {
        return dataEntityService.findEntitiesByProject(projectId);
    }

    /**
     * GET /api/entities/{id} - Lire la configuration d'une entité
     */
    @GetMapping("/entities/{id}")
    public DataEntityResponseDTO getEntity(@PathVariable("id") Long id) {
        return dataEntityService.findById(id);
    }

    /**
     * PUT /api/entities/{id} - Modifier une entité (champs & contraintes)
     */
    @PutMapping("/entities/{id}")
    public DataEntityResponseDTO updateEntity(
            @PathVariable("id") Long id,
            @Valid @RequestBody DataEntityUpdateDTO dto) {
        return dataEntityService.updateEntity(id, dto);
    }

    /**
     * DELETE /api/entities/{id} - Supprimer une entité
     */
    @DeleteMapping("/entities/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntity(@PathVariable("id") Long id) {
        dataEntityService.deleteEntity(id);
    }
}
