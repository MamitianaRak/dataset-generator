package com.example.microservice.service;

import com.example.microservice.dataset.Dataset;
import com.example.microservice.dataset.Record;
import com.example.microservice.dto.generation.EntityDataDTO;
import com.example.microservice.dto.generation.PreviewResponseDTO;
import com.example.microservice.dto.project.ProjectCreateDTO;
import com.example.microservice.dto.project.ProjectResponseDTO;
import com.example.microservice.dto.project.ProjectUpdateDTO;
import com.example.microservice.dto.generation.RecordDTO;
import com.example.microservice.generation.RandomDataGenerator;
import com.example.microservice.generation.interfaces.IDataGenerator;
import com.example.microservice.mapper.ProjectMapper;
import com.example.microservice.model.entities.DataEntity;
import com.example.microservice.model.entities.Project;
import com.example.microservice.repository.ProjectRepository;
import com.example.microservice.util.RecordGenerator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    public List<ProjectResponseDTO> findAll(){
        return projectMapper.projectListToProjectResponseDTOList(projectRepository.findAll());
    }

    public ProjectResponseDTO findById(Long id){
        Project project = this.projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
        return projectMapper.projectToProjectResponseDTO(project);
    }

    @Transactional
    public ProjectResponseDTO save(ProjectCreateDTO projectCreateDTO){
        Project project = projectMapper.projectCreateDTOToProject(projectCreateDTO);
        project = this.projectRepository.save(project);
        return projectMapper.projectToProjectResponseDTO(project);
    }

    @Transactional
    public ProjectResponseDTO update(Long id, ProjectUpdateDTO projectUpdateDTO){
        Project existingProject = this.projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        projectMapper.updateProjectFromDTO(projectUpdateDTO, existingProject);

        Project updatedProject = this.projectRepository.save(existingProject);
        return projectMapper.projectToProjectResponseDTO(updatedProject);
    }

    @Transactional
    public void delete(Long id){
        if (!this.projectRepository.existsById(id)) {
            throw new EntityNotFoundException("Project not found with id: " + id);
        }
        this.projectRepository.deleteById(id);
    }

    @Override
    public PreviewResponseDTO generatePreview(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        // Générer le dataset
        Dataset dataset = generateDatasetForProject(project, new RandomDataGenerator());

        // Convertir Dataset en PreviewResponseDTO
        List<EntityDataDTO> entityDataList = new ArrayList<>();
        for (Map.Entry<String, List<Record>> entry : dataset.getAllRecords().entrySet()) {
            String entityName = entry.getKey();
            List<Record> records = entry.getValue();

            List<RecordDTO> recordDTOs = new ArrayList<>();
            for (Record record : records) {
                RecordDTO recordDTO = RecordDTO.builder()
                        .fields(record.getValues())
                        .build();
                recordDTOs.add(recordDTO);
            }

            EntityDataDTO entityData = EntityDataDTO.builder()
                    .entityName(entityName)
                    .records(recordDTOs)
                    .build();
            entityDataList.add(entityData);
        }

        return PreviewResponseDTO.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .data(entityDataList)
                .build();
    }

    @Override
    public Dataset generateDataset(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        return generateDatasetForProject(project, new RandomDataGenerator());
    }

    /**
     * Génère un dataset pour un projet avec un générateur donné.
     * Logique métier déplacée depuis Project.generate()
     */
    private Dataset generateDatasetForProject(Project project, IDataGenerator generator) {
        Dataset dataset = new Dataset();
        if (generator == null) {
            throw new IllegalArgumentException("Le générateur ne peut pas être null");
        }

        RecordGenerator recordGenerator = new RecordGenerator(generator);

        for (DataEntity entity : project.getEntities()) {
            if (entity == null) continue;
            int count = Math.max(0, entity.getQuantity());
            if (count <= 0) continue;

            List<Map<String, Object>> rows = recordGenerator.generate(entity, count);

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

    /**
     * Génère récursivement les sous-entités.
     * Logique métier déplacée depuis Project.generateSubEntities()
     */
    private void generateSubEntitiesForDataset(DataEntity parentEntity, Dataset dataset, RecordGenerator recordGenerator) {
        for (DataEntity subEntity : parentEntity.getSubEntities()) {
            int count = Math.max(0, subEntity.getQuantity());
            if (count <= 0) continue;

            List<Map<String, Object>> rows = recordGenerator.generate(subEntity, count);
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
