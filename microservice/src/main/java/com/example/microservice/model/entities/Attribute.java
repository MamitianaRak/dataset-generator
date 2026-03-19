package com.example.microservice.model.entities;

import com.example.microservice.distribution.BaseDistribution;
import com.example.microservice.generation.BaseDataGenerator;
import com.example.microservice.model.constraints.BaseConstraint;
import com.example.microservice.model.types.BaseDataType;
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
@Table(name = "Attributes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"data_entity_id", "name"})
})
public class Attribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "data_type_id")
    private BaseDataType type;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
        name = "attribute_constraints",
        joinColumns = @JoinColumn(name = "attribute_id"),
        inverseJoinColumns = @JoinColumn(name = "constraint_id")
    )
    @Builder.Default
    private List<BaseConstraint> constraints = new ArrayList<>();

    private int quantity = 0;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "generator_id")
    private BaseDataGenerator generator;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "distribution_id")
    private BaseDistribution distribution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_entity_id")
    private DataEntity dataEntity;

    public Attribute(String name, BaseDataType type) {
        this.name = name;
        this.type = type;
    }

    public Attribute(String name, BaseDataType type, List<BaseConstraint> constraints) {
        this.name = name;
        this.type = type;
        if (constraints != null) this.constraints = constraints;
    }

}
