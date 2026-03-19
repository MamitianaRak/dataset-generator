package com.example.microservice.controller;

import com.example.microservice.dataset.Dataset;
import com.example.microservice.dto.generation.PreviewResponseDTO;
import com.example.microservice.export.ExporterFactory;
import com.example.microservice.export.interfaces.IDataExporter;
import com.example.microservice.service.DataGenerationService;
import lombok.RequiredArgsConstructor;
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

/**
 * Controller pour la génération et l'export de données.
 */
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectGenerationController {

    private final DataGenerationService dataGenerationService;
    private final ExporterFactory exporterFactory;

    /**
     * Génère un aperçu des données pour un projet.
     * GET /api/projects/{id}/preview
     */
    @GetMapping("/{id}/preview")
    public PreviewResponseDTO generatePreview(@PathVariable("id") Long id) {
        return dataGenerationService.generatePreview(id);
    }

    /**
     * Exporte les données générées dans un format spécifique.
     * GET /api/projects/{id}/export?format=json|xml|sql
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<Resource> exportData(
            @PathVariable("id") Long id,
            @RequestParam(value = "format", defaultValue = "json") String format) throws IOException {

        // Générer le dataset
        Dataset dataset = dataGenerationService.generateDataset(id);

        // Créer un répertoire temporaire pour l'export
        Path tempDir = Files.createTempDirectory("dataset-export-");
        IDataExporter exporter = exporterFactory.getExporter(format);
        MediaType mediaType = exporterFactory.getMediaType(format);
        String fileName = "dataset_" + id + exporterFactory.getExtension(format);
        String filePath = tempDir.resolve(fileName).toString();

        // Exporter les données
        exporter.export(dataset, filePath);

        // Préparer la réponse avec le fichier
        File file = new File(filePath);
        Resource resource = new FileSystemResource(file);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(mediaType)
                .body(resource);
    }
}
