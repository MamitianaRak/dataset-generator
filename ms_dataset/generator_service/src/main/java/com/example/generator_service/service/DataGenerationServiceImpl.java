package com.example.generator_service.service;

import com.example.generator_service.dataset.Dataset;
import com.example.generator_service.dataset.Record;
import com.example.generator_service.dataset.RowSink;
import com.example.generator_service.dto.entity.DataEntityResponseDTO;
import com.example.generator_service.dto.generation.EntityDataDTO;
import com.example.generator_service.dto.generation.PreviewResponseDTO;
import com.example.generator_service.dto.generation.RecordDTO;
import com.example.generator_service.dto.project.ProjectResponseDTO;
import com.example.generator_service.feign.DatasetManagerClient;
import com.example.generator_service.feign.DatasetManagerUnavailableException;
import com.example.generator_service.generation.FakerDataGenerator;
import com.example.generator_service.generation.RelationalDatasetBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataGenerationServiceImpl implements DataGenerationService {

    private final DatasetManagerClient datasetManagerClient;
    private final int previewRows;

    public DataGenerationServiceImpl(DatasetManagerClient datasetManagerClient,
                                     @Value("${app.preview.rows-per-table:100}") int previewRows) {
        this.datasetManagerClient = datasetManagerClient;
        this.previewRows = previewRows;
    }

    @Override
    public PreviewResponseDTO generatePreview(Long projectId) {
        log.info("Generating preview for project ID: {}", projectId);

        ProjectResponseDTO project;
        List<DataEntityResponseDTO> entities;
        try {
            project = datasetManagerClient.getProject(projectId);
            entities = datasetManagerClient.getProjectEntities(projectId);
        } catch (DatasetManagerUnavailableException e) {
            // Fallback (circuit breaker): a partial answer instead of an error. An unknown project still gives 404.
            return PreviewResponseDTO.builder()
                    .projectId(projectId)
                    .projectName("Not available")
                    .status("PARTIAL")
                    .message(e.getMessage())
                    .data(List.of())
                    .build();
        }

        // Capped: a table of a million rows previews in the same time as one of ten.
        Dataset dataset = builder().preview(entities, previewRows);

        List<EntityDataDTO> entityDataList = new ArrayList<>();
        for (Map.Entry<String, List<Record>> entry : dataset.getAllRecords().entrySet()) {
            List<RecordDTO> recordDTOs = new ArrayList<>();
            for (Record record : entry.getValue()) {
                recordDTOs.add(RecordDTO.builder().fields(record.getValues()).build());
            }
            entityDataList.add(EntityDataDTO.builder()
                    .entityName(entry.getKey())
                    .records(recordDTOs)
                    .build());
        }

        return PreviewResponseDTO.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .status("OK")
                .data(entityDataList)
                .build();
    }

    /**
     * Throws {@link DatasetManagerUnavailableException} on an outage (503) and {@code ProjectNotFoundException} for
     * an unknown project (404): an export never sends an empty file in place of an error.
     */
    @Override
    public List<DataEntityResponseDTO> loadEntities(Long projectId) {
        log.info("Loading entities of project ID: {}", projectId);
        return datasetManagerClient.getProjectEntities(projectId);
    }

    @Override
    public void generate(List<DataEntityResponseDTO> entities, RowSink sink) {
        builder().generate(entities, Integer.MAX_VALUE, sink);
    }

    /** Parents first, each sub-entity row referencing an existing parent row (see {@link RelationalDatasetBuilder}). */
    private static RelationalDatasetBuilder builder() {
        return new RelationalDatasetBuilder(new FakerDataGenerator(), RandomGenerator.getDefault());
    }
}
