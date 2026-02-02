package com.example.microservice.mapper;

import com.example.microservice.dto.project.ProjectCreateDTO;
import com.example.microservice.dto.project.ProjectResponseDTO;
import com.example.microservice.dto.project.ProjectUpdateDTO;
import com.example.microservice.model.entities.Project;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProjectMapper {
    ProjectResponseDTO projectToProjectResponseDTO(Project project);
    Project projectCreateDTOToProject(ProjectCreateDTO projectCreateDTO);
    void updateProjectFromDTO(ProjectUpdateDTO projectUpdateDTO, @MappingTarget Project project);
    List<ProjectResponseDTO> projectListToProjectResponseDTOList(List<Project> projectList);
}
