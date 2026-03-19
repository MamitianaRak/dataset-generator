package com.example.microservice.repository;

import com.example.microservice.model.entities.DataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataEntityRepository extends JpaRepository<DataEntity, Long> {
    /**
     * Trouve toutes les entités d'un projet donné.
     */
    List<DataEntity> findByProjectId(Long projectId);

    /**
     * Trouve les entités racines d'un projet (sans parent).
     */
    List<DataEntity> findByProjectIdAndParentEntityIsNull(Long projectId);
}
