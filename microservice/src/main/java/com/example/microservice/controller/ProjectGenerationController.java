package com.example.microservice.controller;

import com.example.microservice.dataset.Dataset;
import com.example.microservice.dto.generation.PreviewResponseDTO;
import com.example.microservice.export.JSONExporter;
import com.example.microservice.export.SQLExporter;
import com.example.microservice.export.XMLExporter;
import com.example.microservice.export.interfaces.IDataExporter;
import com.example.microservice.service.ProjectService;
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
import java.nio.file.Paths;

/**
 * Controller pour la génération et l'export de données.
 */
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectGenerationController {

    private final ProjectService projectService;

    /**
     * Génère un aperçu des données pour un projet.
     * GET /api/projects/{id}/preview
     */
    @GetMapping("/{id}/preview")
    public PreviewResponseDTO generatePreview(@PathVariable("id") Long id) {
        return projectService.generatePreview(id);
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
        Dataset dataset = projectService.generateDataset(id);

        // Créer un répertoire temporaire pour l'export
        Path tempDir = Files.createTempDirectory("dataset-export-");
        String fileName = "dataset_" + id;
        String filePath;
        IDataExporter exporter;
        MediaType mediaType;

        // Choisir l'exporteur selon le format (JSON, XML, SQL uniquement)
        switch (format.toLowerCase()) {
            case "xml":
                fileName += ".xml";
                filePath = tempDir.resolve(fileName).toString();
                exporter = new XMLExporter();
                mediaType = MediaType.APPLICATION_XML;
                break;
            case "sql":
                fileName += ".sql";
                filePath = tempDir.resolve(fileName).toString();
                exporter = new SQLExporter();
                mediaType = MediaType.parseMediaType("application/sql");
                break;
            case "json":
            default:
                fileName += ".json";
                filePath = tempDir.resolve(fileName).toString();
                exporter = new JSONExporter();
                mediaType = MediaType.APPLICATION_JSON;
                break;
        }

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
