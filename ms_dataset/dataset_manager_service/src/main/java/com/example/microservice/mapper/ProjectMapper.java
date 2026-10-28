package com.example.microservice.mapper;

import com.example.microservice.dto.project.ProjectResponseDTO;
import com.example.microservice.model.entities.Project;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Entity to response only: creation and renaming go through the entity's own methods. */
@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(source = "createdAt", target = "dateCreated")
    @Mapping(source = "updatedAt", target = "lastUpdated")
    ProjectResponseDTO projectToProjectResponseDTO(Project project);

    List<ProjectResponseDTO> projectListToProjectResponseDTOList(List<Project> projectList);
}
