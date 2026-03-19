package com.example.microservice.model.constraints;

import com.example.microservice.model.interfaces.IConstraint;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Constraints")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "constraint_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class BaseConstraint implements IConstraint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
