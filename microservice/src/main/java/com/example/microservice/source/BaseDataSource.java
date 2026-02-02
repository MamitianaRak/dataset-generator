package com.example.microservice.source;

import com.example.microservice.source.interfaces.IDataSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DataSources")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "source_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class BaseDataSource implements IDataSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
