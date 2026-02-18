package com.example.microservice.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "DataEntities", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_id", "name"})
})
public class DataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int quantity = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "dataEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Attribute> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "parentEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DataEntity> subEntities = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_entity_id")
    private DataEntity parentEntity;

    public DataEntity(String name) {
        this.name = name;
        this.attributes = new ArrayList<>();
        this.subEntities = new ArrayList<>();
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
        attribute.setDataEntity(this);
    }

    public void addSubEntity(DataEntity entity) {
        this.subEntities.add(entity);
        entity.setParentEntity(this);
    }
}
