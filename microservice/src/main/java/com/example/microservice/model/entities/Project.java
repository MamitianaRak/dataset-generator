package com.example.microservice.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Entité JPA représentant un projet de génération de dataset.
 * Contient des entités de données (DataEntity).
 * La logique de génération et d'export est gérée par ProjectService.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "Projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DataEntity> entities = new ArrayList<>();

    @CreationTimestamp
    private Date dateCreated;

    @UpdateTimestamp
    private Date lastUpdated;

    public Project(String name) {
        this.name = name;
        this.entities = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DataEntity> getEntities() {
        return new ArrayList<>(entities);
    }

    public void setEntities(List<DataEntity> entities) {
        this.entities = entities == null ? new ArrayList<>() : new ArrayList<>(entities);
    }

    public void addEntity(DataEntity entity) {
        if (entity == null) return;
        this.entities.add(entity);
        entity.setProject(this);
    }

    public boolean removeEntity(DataEntity entity) {
        if (entity == null) return false;
        return this.entities.remove(entity);
    }

    public DataEntity findEntityByName(String name) {
        if (name == null) return null;
        for (DataEntity e : entities) {
            if (name.equals(e.getName())) return e;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", entitiesCount=" + (entities == null ? 0 : entities.size()) +
                '}';
    }
}
