package com.example.microservice.generation;

import com.example.microservice.generation.interfaces.IDataGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DataGenerators")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "generator_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class BaseDataGenerator implements IDataGenerator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
