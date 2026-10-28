package com.example.microservice.repository;

import com.example.microservice.model.entities.DataEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DataEntityRepository extends JpaRepository<DataEntity, Long> {

    /**
     * Every entity of a project, sub-entities included, with their attributes: one query for the whole project.
     * The tree is rebuilt in memory from the parent ids.
     */
    @Query("""
            select e from DataEntity e
            left join fetch e.attributes
            where e.project.id = :projectId
            order by e.id""")
    List<DataEntity> findAllWithAttributesByProjectId(@Param("projectId") Long projectId);

    @Query("select e from DataEntity e left join fetch e.attributes where e.id = :id")
    Optional<DataEntity> findWithAttributesById(@Param("id") Long id);
}
