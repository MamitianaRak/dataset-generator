package com.example.generator_service.controller;

import com.example.generator_service.dto.entity.DataEntityResponseDTO;
import com.example.generator_service.dto.generation.PreviewResponseDTO;
import com.example.generator_service.export.ExporterFactory;
import com.example.generator_service.export.interfaces.IDataExporter;
import com.example.generator_service.export.interfaces.IDataExporter.TableWriter;
import com.example.generator_service.service.DataGenerationService;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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

    /**
     * Streams the export: rows are written to the response as they are generated, with no temporary file and
     * nothing kept in memory. The format and the project are checked first, while an error can still be returned
     * with its status; once rows are flowing, the response is committed.
     */
    @GetMapping("/{projectId}/export")
    public ResponseEntity<StreamingResponseBody> exportData(
            @PathVariable("projectId") Long projectId,
            @RequestParam(value = "format", defaultValue = "json") String format) {

        log.info("Received export request for project ID: {} with format: {}", projectId, format);

        IDataExporter exporter = exporterFactory.getExporter(format);
        List<DataEntityResponseDTO> entities = dataGenerationService.loadEntities(projectId);
        String fileName = "dataset_" + projectId + exporterFactory.getExtension(format);

        StreamingResponseBody body = output -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), 64 * 1024);
            TableWriter tables = exporter.open(writer);
            dataGenerationService.generate(entities, tables);
            tables.finish();
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(exporterFactory.getMediaType(format))
                .body(body);
    }
}
