package com.example.microservice.controller;

import com.example.microservice.dto.project.ProjectCreateDTO;
import com.example.microservice.dto.project.ProjectResponseDTO;
import com.example.microservice.dto.project.ProjectUpdateDTO;
import com.example.microservice.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponseDTO> findAll() {
        return projectService.findAll();
    }

    @GetMapping("/{id}")
    public ProjectResponseDTO findById(@PathVariable("id") Long id) {
        ProjectResponseDTO project = projectService.findById(id);
        if (project == null) {
            throw new EntityNotFoundException("Project not found with id: " + id);
        }
        return project;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponseDTO create(@RequestBody ProjectCreateDTO project) {
        return projectService.save(project);
    }

    @PutMapping("/{id}")
    public ProjectResponseDTO update(@PathVariable("id") Long id, @RequestBody ProjectUpdateDTO project) {
        return projectService.update(id, project);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        projectService.delete(id);
    }

}
