package com.example.microservice.model.types;

import com.example.microservice.model.interfaces.IDataType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DataTypes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class BaseDataType implements IDataType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
