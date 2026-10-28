package com.example.microservice.model.entities;

import com.example.microservice.model.schema.AttributeDefinition;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * A table to generate. Sub-entities belong to the same project as their parent, so the whole project loads in one
 * query and entity names are unique per project (they become table names in the SQL export).
 */
@Entity
@Table(name = "data_entity")
public class DataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_entity_seq")
    @SequenceGenerator(name = "data_entity_seq", sequenceName = "data_entity_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DataEntity parent;

    @Column(nullable = false)
    private String name;

    private int quantity;

    @Version
    private long version;

    @OneToMany(mappedBy = "dataEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<Attribute> attributes = new ArrayList<>();

    protected DataEntity() {
        // for JPA
    }

    public DataEntity(Project project, DataEntity parent, String name, int quantity) {
        this.project = Objects.requireNonNull(project, "project");
        this.parent = parent;
        this.name = Objects.requireNonNull(name, "name");
        this.quantity = quantity;
    }

    public void rename(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Replaces the attributes, matching them by name: a kept attribute is updated in place (same id), a new one is
     * added, a missing one is removed. Deleting everything and inserting again would hit the unique (entity, name)
     * constraint, because Hibernate flushes inserts before deletes.
     */
    public void defineAttributes(Map<String, AttributeSpec> specsByName) {
        Set<String> wanted = new HashSet<>(specsByName.keySet());
        attributes.removeIf(attribute -> !wanted.contains(attribute.getName()));
        Map<String, Attribute> current = new LinkedHashMap<>();
        attributes.forEach(attribute -> current.put(attribute.getName(), attribute));
        specsByName.forEach((name, spec) -> {
            Attribute existing = current.get(name);
            if (existing != null) {
                existing.redefine(spec.quantity(), spec.definition());
            } else {
                attributes.add(new Attribute(this, name, spec.quantity(), spec.definition()));
            }
        });
    }

    /** What an attribute should become, keyed by its name in {@link #defineAttributes}. */
    public record AttributeSpec(int quantity, AttributeDefinition definition) {
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    /** The parent's id, read from the lazy reference without loading the parent. */
    public Long getParentId() {
        return parent == null ? null : parent.getId();
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof DataEntity other && id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return DataEntity.class.hashCode();
    }

    @Override
    public String toString() {
        return "DataEntity{id=" + id + ", name='" + name + "'}";
    }
}
