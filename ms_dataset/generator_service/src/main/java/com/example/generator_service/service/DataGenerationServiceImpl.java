package com.example.generator_service.service;

import com.example.generator_service.dataset.Dataset;
import com.example.generator_service.dataset.Record;
import com.example.generator_service.dto.entity.DataEntityResponseDTO;
import com.example.generator_service.dto.generation.EntityDataDTO;
import com.example.generator_service.dto.generation.PreviewResponseDTO;
import com.example.generator_service.dto.generation.RecordDTO;
import com.example.generator_service.dto.project.ProjectResponseDTO;
import com.example.generator_service.feign.DatasetManagerClient;
import com.example.generator_service.generation.FakerDataGenerator;
import com.example.generator_service.generation.interfaces.IDataGenerator;
import com.example.generator_service.util.RecordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataGenerationServiceImpl implements DataGenerationService {

    private final DatasetManagerClient datasetManagerClient;

    @Override
    public PreviewResponseDTO generatePreview(Long projectId) {
        log.info("Generating preview for project ID: {}", projectId);

        ProjectResponseDTO project = datasetManagerClient.getProject(projectId);
        List<DataEntityResponseDTO> entities = datasetManagerClient.getProjectEntities(projectId);

        // Si fallback déclenché (project null), retourner réponse partielle
        if (project == null) {
            log.warn("Circuit breaker triggered: dataset-manager is unavailable");
            return PreviewResponseDTO.builder()
                    .projectId(projectId)
                    .projectName("Not available")
                    .status("PARTIAL")
                    .message("Le service dataset-manager est indisponible. Veuillez réessayer plus tard.")
                    .data(List.of())
                    .build();
        }

        Dataset dataset = generateDatasetFromEntities(entities, new FakerDataGenerator());

        List<EntityDataDTO> entityDataList = new ArrayList<>();
        for (Map.Entry<String, List<Record>> entry : dataset.getAllRecords().entrySet()) {
            String entityName = entry.getKey();
            List<Record> records = entry.getValue();

            List<RecordDTO> recordDTOs = new ArrayList<>();
            for (Record record : records) {
                recordDTOs.add(RecordDTO.builder().fields(record.getValues()).build());
            }

            entityDataList.add(EntityDataDTO.builder()
                    .entityName(entityName)
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

    @Override
    public Dataset generateDataset(Long projectId) {
        log.info("Generating dataset for project ID: {}", projectId);

        List<DataEntityResponseDTO> entities = datasetManagerClient.getProjectEntities(projectId);
        return generateDatasetFromEntities(entities, new FakerDataGenerator());
    }

    private Dataset generateDatasetFromEntities(List<DataEntityResponseDTO> entities, IDataGenerator generator) {
        Dataset dataset = new Dataset();
        RecordGenerator recordGenerator = new RecordGenerator(generator);

        for (DataEntityResponseDTO entity : entities) {
            if (entity == null) continue;
            int count = Math.max(0, entity.getQuantity());
            if (count <= 0) continue;

            List<Map<String, Object>> rows = recordGenerator.generateFromDTO(entity, count);
            List<Record> records = new ArrayList<>(rows.size());
            for (Map<String, Object> row : rows) {
                Record r = new Record();
                for (Map.Entry<String, Object> col : row.entrySet()) {
                    r.setValue(col.getKey(), col.getValue());
                }
                records.add(r);
            }
            dataset.addRecords(entity.getName(), records);
            generateSubEntitiesForDataset(entity, dataset, recordGenerator);
        }

        return dataset;
    }

    private void generateSubEntitiesForDataset(DataEntityResponseDTO parentEntity, Dataset dataset, RecordGenerator recordGenerator) {
        if (parentEntity.getSubEntities() == null) return;

        for (DataEntityResponseDTO subEntity : parentEntity.getSubEntities()) {
            int count = Math.max(0, subEntity.getQuantity());
            if (count <= 0) continue;

            List<Map<String, Object>> rows = recordGenerator.generateFromDTO(subEntity, count);
            List<Record> records = new ArrayList<>(rows.size());
            for (Map<String, Object> row : rows) {
                Record r = new Record();
                for (Map.Entry<String, Object> col : row.entrySet()) {
                    r.setValue(col.getKey(), col.getValue());
                }
                records.add(r);
            }
            dataset.addRecords(subEntity.getName(), records);
            generateSubEntitiesForDataset(subEntity, dataset, recordGenerator);
        }
    }
}
