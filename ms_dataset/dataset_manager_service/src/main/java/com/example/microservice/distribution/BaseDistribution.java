package com.example.microservice.distribution;

import com.example.microservice.distribution.interfaces.IDistribution;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Distributions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "distribution_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class BaseDistribution implements IDistribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
