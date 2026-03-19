package com.example.microservice.service;

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
}
