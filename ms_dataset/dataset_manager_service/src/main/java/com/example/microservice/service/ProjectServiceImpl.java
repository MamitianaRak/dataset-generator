package com.example.microservice.service;

import com.example.microservice.dto.project.ProjectCreateDTO;
import com.example.microservice.dto.project.ProjectResponseDTO;
import com.example.microservice.dto.project.ProjectUpdateDTO;
import com.example.microservice.mapper.ProjectMapper;
import com.example.microservice.model.entities.Project;
import com.example.microservice.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    public List<ProjectResponseDTO> findAll() {
        return projectMapper.projectListToProjectResponseDTOList(projectRepository.findAll());
    }

    @Override
    public ProjectResponseDTO findById(Long id) {
        return projectMapper.projectToProjectResponseDTO(load(id));
    }

    @Override
    @Transactional
    public ProjectResponseDTO save(ProjectCreateDTO projectCreateDTO) {
        Project project = projectRepository.saveAndFlush(new Project(projectCreateDTO.getName()));
        return projectMapper.projectToProjectResponseDTO(project);
    }

    @Override
    @Transactional
    public ProjectResponseDTO update(Long id, ProjectUpdateDTO projectUpdateDTO) {
        Project project = load(id);
        if (projectUpdateDTO.getName() != null) {
            project.rename(projectUpdateDTO.getName());
        }
        // Flushed here so the new timestamps and a duplicate name show up in this response, not after it.
        return projectMapper.projectToProjectResponseDTO(projectRepository.saveAndFlush(project));
    }

    /** The database removes the project's entities and attributes ({@code on delete cascade}). */
    @Override
    @Transactional
    public void delete(Long id) {
        projectRepository.delete(load(id));
    }

    private Project load(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
    }
}
