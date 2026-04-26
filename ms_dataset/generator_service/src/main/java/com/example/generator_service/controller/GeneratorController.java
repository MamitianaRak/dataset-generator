package com.example.generator_service.controller;

import com.example.generator_service.dataset.Dataset;
import com.example.generator_service.dto.generation.PreviewResponseDTO;
import com.example.generator_service.export.ExporterFactory;
import com.example.generator_service.export.interfaces.IDataExporter;
import com.example.generator_service.service.DataGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RestController
@RequestMapping("/api/generator")
@RequiredArgsConstructor
@Slf4j
public class GeneratorController {

    private final DataGenerationService dataGenerationService;
    private final ExporterFactory exporterFactory;

    @GetMapping("/{projectId}/preview")
    public ResponseEntity<PreviewResponseDTO> generatePreview(@PathVariable("projectId") Long projectId) {
        log.info("Received preview request for project ID: {}", projectId);
        PreviewResponseDTO preview = dataGenerationService.generatePreview(projectId);
        return ResponseEntity.ok(preview);
    }

    @GetMapping("/{projectId}/export")
    public ResponseEntity<Resource> exportData(
            @PathVariable("projectId") Long projectId,
            @RequestParam(value = "format", defaultValue = "json") String format) {

        log.info("Received export request for project ID: {} with format: {}", projectId, format);

        Dataset dataset = dataGenerationService.generateDataset(projectId);

        try {
            Path tempDir = Files.createTempDirectory("dataset-export-");
            IDataExporter exporter = exporterFactory.getExporter(format);
            MediaType mediaType = exporterFactory.getMediaType(format);
            String fileName = "dataset_" + projectId + exporterFactory.getExtension(format);
            String filePath = tempDir.resolve(fileName).toString();

            exporter.export(dataset, filePath);

            File file = new File(filePath);
            Resource resource = new FileSystemResource(file);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(mediaType)
                    .body(resource);
        } catch (IOException e) {
            log.error("Error exporting data for project {}: {}", projectId, e.getMessage());
            throw new RuntimeException("Error exporting data", e);
        }
    }
}
