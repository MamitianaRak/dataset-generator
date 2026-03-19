package com.example.microservice.service;

import com.example.microservice.dto.project.ProjectCreateDTO;
import com.example.microservice.dto.project.ProjectResponseDTO;
import com.example.microservice.dto.project.ProjectUpdateDTO;
import com.example.microservice.mapper.ProjectMapper;
import com.example.microservice.model.entities.Project;
import com.example.microservice.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    public List<ProjectResponseDTO> findAll(){
        return projectMapper.projectListToProjectResponseDTOList(projectRepository.findAll());
    }

    public ProjectResponseDTO findById(Long id){
        Project project = this.projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
        return projectMapper.projectToProjectResponseDTO(project);
    }

    @Transactional
    public ProjectResponseDTO save(ProjectCreateDTO projectCreateDTO){
        Project project = projectMapper.projectCreateDTOToProject(projectCreateDTO);
        project = this.projectRepository.save(project);
        return projectMapper.projectToProjectResponseDTO(project);
    }

    @Transactional
    public ProjectResponseDTO update(Long id, ProjectUpdateDTO projectUpdateDTO){
        Project existingProject = this.projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        projectMapper.updateProjectFromDTO(projectUpdateDTO, existingProject);

        Project updatedProject = this.projectRepository.save(existingProject);
        return projectMapper.projectToProjectResponseDTO(updatedProject);
    }

    @Transactional
    public void delete(Long id){
        if (!this.projectRepository.existsById(id)) {
            throw new EntityNotFoundException("Project not found with id: " + id);
        }
        this.projectRepository.deleteById(id);
    }
}
