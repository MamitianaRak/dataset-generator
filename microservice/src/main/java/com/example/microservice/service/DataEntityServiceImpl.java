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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DataEntityServiceImpl implements DataEntityService {

    private final DataEntityRepository dataEntityRepository;
    private final ProjectRepository projectRepository;
    private final DataEntityMapper dataEntityMapper;

    @Override
    public DataEntityResponseDTO createEntity(Long projectId, DataEntityCreateDTO dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        DataEntity entity = dataEntityMapper.toEntity(dto);
        entity.setProject(project);

        DataEntity savedEntity = dataEntityRepository.save(entity);
        return dataEntityMapper.toResponseDTO(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataEntityResponseDTO> findEntitiesByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException("Project not found with id: " + projectId);
        }

        List<DataEntity> entities = dataEntityRepository.findByProjectIdAndParentEntityIsNull(projectId);
        return entities.stream()
                .map(dataEntityMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DataEntityResponseDTO findById(Long id) {
        DataEntity entity = dataEntityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DataEntity not found with id: " + id));
        return dataEntityMapper.toResponseDTO(entity);
    }

    @Override
    public DataEntityResponseDTO updateEntity(Long id, DataEntityUpdateDTO dto) {
        DataEntity entity = dataEntityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DataEntity not found with id: " + id));

        dataEntityMapper.updateEntityFromDTO(dto, entity);
        DataEntity updatedEntity = dataEntityRepository.save(entity);
        return dataEntityMapper.toResponseDTO(updatedEntity);
    }

    @Override
    public void deleteEntity(Long id) {
        if (!dataEntityRepository.existsById(id)) {
            throw new EntityNotFoundException("DataEntity not found with id: " + id);
        }
        dataEntityRepository.deleteById(id);
    }
}
