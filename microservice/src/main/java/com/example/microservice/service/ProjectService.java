package com.example.microservice.service;

import com.example.microservice.dataset.Dataset;
import com.example.microservice.dto.generation.PreviewResponseDTO;
import com.example.microservice.dto.project.ProjectCreateDTO;
import com.example.microservice.dto.project.ProjectResponseDTO;
import com.example.microservice.dto.project.ProjectUpdateDTO;

import java.util.List;

public interface ProjectService {
    List<ProjectResponseDTO> findAll();
    ProjectResponseDTO findById(Long id);
    ProjectResponseDTO save(ProjectCreateDTO project);
    ProjectResponseDTO update(Long id, ProjectUpdateDTO project);
    void delete(Long id);

    // Génération de données
    PreviewResponseDTO generatePreview(Long projectId);
    Dataset generateDataset(Long projectId);
}
