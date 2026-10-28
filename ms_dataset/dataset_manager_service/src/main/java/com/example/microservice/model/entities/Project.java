package com.example.microservice.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * A dataset project. Its entities are loaded through {@code DataEntityRepository} in one query rather than through
 * a collection here, and are removed by the database ({@code on delete cascade}) when the project is deleted.
 */
@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_seq")
    @SequenceGenerator(name = "project_seq", sequenceName = "project_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /** Optimistic lock: two concurrent updates cannot silently overwrite each other. */
    @Version
    private long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    protected Project() {
        // for JPA
    }

    public Project(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public void rename(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Identity is the database id: a new, unsaved project equals only itself. */
    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Project other && id != null && id.equals(other.getId());
    }

    /** Constant per class, so the hash does not change when the id is assigned on persist. */
    @Override
    public int hashCode() {
        return Project.class.hashCode();
    }

    @Override
    public String toString() {
        return "Project{id=" + id + ", name='" + name + "'}";
    }
}
