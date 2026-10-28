package com.example.microservice.model.entities;

import com.example.microservice.model.schema.AttributeDefinition;
import com.example.microservice.model.schema.AttributeDefinitionConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

/** A column of a generated table. Its type and constraints are one JSON value, read with the row: no join. */
@Entity
@Table(name = "attribute")
public class Attribute {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attribute_seq")
    @SequenceGenerator(name = "attribute_seq", sequenceName = "attribute_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "data_entity_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DataEntity dataEntity;

    @Column(nullable = false)
    private String name;

    private int quantity;

    @Convert(converter = AttributeDefinitionConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private AttributeDefinition definition;

    protected Attribute() {
        // for JPA
    }

    Attribute(DataEntity dataEntity, String name, int quantity, AttributeDefinition definition) {
        this.dataEntity = Objects.requireNonNull(dataEntity, "dataEntity");
        this.name = Objects.requireNonNull(name, "name");
        redefine(quantity, definition);
    }

    void redefine(int quantity, AttributeDefinition definition) {
        this.quantity = quantity;
        this.definition = Objects.requireNonNull(definition, "definition");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public AttributeDefinition getDefinition() {
        return definition;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Attribute other && id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Attribute.class.hashCode();
    }
}
