package com.example.microservice.service;

import com.example.microservice.dto.entity.DataEntityCreateDTO;
import com.example.microservice.dto.entity.DataEntityResponseDTO;
import com.example.microservice.dto.entity.DataEntityUpdateDTO;
import com.example.microservice.mapper.DataEntityMapper;
import com.example.microservice.model.entities.DataEntity;
import com.example.microservice.model.entities.Project;
import com.example.microservice.repository.DataEntityRepository;
import com.example.microservice.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
class DataEntityServiceImpl implements DataEntityService {

    private final DataEntityRepository dataEntityRepository;
    private final ProjectRepository projectRepository;
    private final DataEntityMapper dataEntityMapper;

    /** Saves the entity and its sub-entities; with sequence ids, Hibernate sends the inserts in JDBC batches. */
    @Override
    public DataEntityResponseDTO createEntity(Long projectId, DataEntityCreateDTO dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));
        List<DataEntity> created = new ArrayList<>();
        DataEntity root = create(project, null, dto, created);
        dataEntityRepository.saveAll(created);
        // Flushed here so a duplicate name fails this call (409) instead of the commit after it.
        dataEntityRepository.flush();
        return dataEntityMapper.toTrees(created, entity -> entity == root).getFirst();
    }

    private DataEntity create(Project project, DataEntity parent, DataEntityCreateDTO dto, List<DataEntity> created) {
        DataEntity entity = new DataEntity(project, parent, dto.getName(), dto.getQuantity());
        entity.defineAttributes(dataEntityMapper.toSpecs(dto.getAttributes()));
        created.add(entity);
        if (dto.getSubEntities() != null) {
            dto.getSubEntities().forEach(child -> create(project, entity, child, created));
        }
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataEntityResponseDTO> findEntitiesByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException("Project not found with id: " + projectId);
        }
        List<DataEntity> all = dataEntityRepository.findAllWithAttributesByProjectId(projectId);
        return dataEntityMapper.toTrees(all, entity -> entity.getParentId() == null);
    }

    @Override
    @Transactional(readOnly = true)
    public DataEntityResponseDTO findById(Long id) {
        return subtree(load(id));
    }

    /** Sub-entities are not part of the update request: they are kept as they are. */
    @Override
    public DataEntityResponseDTO updateEntity(Long id, DataEntityUpdateDTO dto) {
        DataEntity entity = dataEntityRepository.findWithAttributesById(id)
                .orElseThrow(() -> notFound(id));
        if (dto.getName() != null) {
            entity.rename(dto.getName());
        }
        entity.setQuantity(dto.getQuantity());
        entity.defineAttributes(dataEntityMapper.toSpecs(dto.getAttributes()));
        dataEntityRepository.flush();
        return subtree(entity);
    }

    /** The database removes the attributes and sub-entities ({@code on delete cascade}). */
    @Override
    public void deleteEntity(Long id) {
        dataEntityRepository.delete(load(id));
    }

    /** The entity with its descendants, from one query on its project. */
    private DataEntityResponseDTO subtree(DataEntity entity) {
        List<DataEntity> all = dataEntityRepository.findAllWithAttributesByProjectId(entity.getProject().getId());
        return dataEntityMapper.toTrees(all, candidate -> Objects.equals(candidate.getId(), entity.getId()))
                .getFirst();
    }

    private DataEntity load(Long id) {
        return dataEntityRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private static EntityNotFoundException notFound(Long id) {
        return new EntityNotFoundException("DataEntity not found with id: " + id);
    }
}
