package com.example.generator_service.feign;

import com.example.generator_service.dto.entity.DataEntityResponseDTO;
import com.example.generator_service.dto.project.ProjectResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "dataset-manager", fallbackFactory = DatasetManagerClientFallback.class)
public interface DatasetManagerClient {

    @GetMapping("/api/projects/{projectId}")
    ProjectResponseDTO getProject(@PathVariable("projectId") Long projectId);

    @GetMapping("/api/projects/{projectId}/entities")
    List<DataEntityResponseDTO> getProjectEntities(@PathVariable("projectId") Long projectId);
}
