package com.example.microservice.mapper;

import com.example.microservice.dto.entity.AttributeDTO;
import com.example.microservice.dto.entity.DataEntityCreateDTO;
import com.example.microservice.dto.entity.DataEntityResponseDTO;
import com.example.microservice.dto.entity.DataEntityUpdateDTO;
import com.example.microservice.dto.model.constraints.ConstraintDTO;
import com.example.microservice.dto.model.constraints.EnumConstraintDTO;
import com.example.microservice.dto.model.constraints.MinMaxConstraintDTO;
import com.example.microservice.dto.model.generators.EmailGeneratorDTO;
import com.example.microservice.dto.model.generators.GeneratorDTO;
import com.example.microservice.dto.model.generators.RandomGeneratorDTO;
import com.example.microservice.dto.model.generators.SequentialGeneratorDTO;
import com.example.microservice.dto.model.types.*;
import com.example.microservice.generation.BaseDataGenerator;
import com.example.microservice.generation.EmailDataGenerator;
import com.example.microservice.generation.RandomDataGenerator;
import com.example.microservice.generation.SequentialDataGenerator;
import com.example.microservice.model.constraints.BaseConstraint;
import com.example.microservice.model.constraints.EnumConstraint;
import com.example.microservice.model.constraints.MinMaxConstraint;
import com.example.microservice.model.entities.Attribute;
import com.example.microservice.model.entities.DataEntity;
import com.example.microservice.model.types.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour DataEntity et Attribute.
 * Gère la conversion entre DTOs et entités avec polymorphie.
 */
@Component
public class DataEntityMapper {

    public DataEntityResponseDTO toResponseDTO(DataEntity entity) {
        if (entity == null) return null;

        return DataEntityResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .quantity(entity.getQuantity())
                .projectId(entity.getProject() != null ? entity.getProject().getId() : null)
                .attributes(entity.getAttributes() != null ?
                    entity.getAttributes().stream().map(this::toAttributeDTO).collect(Collectors.toList()) :
                    new ArrayList<>())
                .subEntities(entity.getSubEntities() != null ?
                    entity.getSubEntities().stream().map(this::toResponseDTO).collect(Collectors.toList()) :
                    new ArrayList<>())
                .build();
    }

    public DataEntity toEntity(DataEntityCreateDTO dto) {
        if (dto == null) return null;

        DataEntity entity = new DataEntity();
        entity.setName(dto.getName());
        entity.setQuantity(dto.getQuantity());

        if (dto.getAttributes() != null) {
            List<Attribute> attributes = dto.getAttributes().stream()
                    .map(this::toAttribute)
                    .collect(Collectors.toList());
            attributes.forEach(entity::addAttribute);
        }

        if (dto.getSubEntities() != null) {
            List<DataEntity> subEntities = dto.getSubEntities().stream()
                    .map(this::toEntity)
                    .collect(Collectors.toList());
            subEntities.forEach(entity::addSubEntity);
        }

        return entity;
    }

    public void updateEntityFromDTO(DataEntityUpdateDTO dto, DataEntity entity) {
        if (dto == null || entity == null) return;

        entity.setName(dto.getName());
        entity.setQuantity(dto.getQuantity());

        if (dto.getAttributes() != null) {
            entity.getAttributes().clear();
            dto.getAttributes().stream()
                    .map(this::toAttribute)
                    .forEach(entity::addAttribute);
        }
    }

    private AttributeDTO toAttributeDTO(Attribute attribute) {
        if (attribute == null) return null;

        return AttributeDTO.builder()
                .id(attribute.getId())
                .name(attribute.getName())
                .type(toDataTypeDTO(attribute.getType()))
                .constraints(attribute.getConstraints() != null ?
                    attribute.getConstraints().stream().map(this::toConstraintDTO).collect(Collectors.toList()) :
                    new ArrayList<>())
                .quantity(attribute.getQuantity())
                .generator(toGeneratorDTO(attribute.getGenerator()))
                .build();
    }

    private Attribute toAttribute(AttributeDTO dto) {
        if (dto == null) return null;

        Attribute attribute = new Attribute();
        attribute.setName(dto.getName());
        attribute.setType(toDataType(dto.getType()));
        attribute.setQuantity(dto.getQuantity());
        attribute.setGenerator(toGenerator(dto.getGenerator()));

        if (dto.getConstraints() != null) {
            List<BaseConstraint> constraints = dto.getConstraints().stream()
                    .map(this::toConstraint)
                    .collect(Collectors.toList());
            attribute.setConstraints(constraints);
        }

        return attribute;
    }

    // Conversion DataType
    private DataTypeDTO toDataTypeDTO(BaseDataType type) {
        if (type == null) return null;

        if (type instanceof IntegerType) {
            return new IntegerTypeDTO();
        } else if (type instanceof FloatType) {
            return new FloatTypeDTO();
        } else if (type instanceof StringType) {
            return new StringTypeDTO();
        } else if (type instanceof BooleanType) {
            return new BooleanTypeDTO();
        } else if (type instanceof DateType) {
            return new DateTypeDTO();
        } else if (type instanceof EnumType) {
            EnumTypeDTO dto = new EnumTypeDTO();
            dto.setValues(((EnumType) type).getValues());
            return dto;
        }
        return null;
    }

    private BaseDataType toDataType(DataTypeDTO dto) {
        if (dto == null) return null;

        if (dto instanceof IntegerTypeDTO) {
            return new IntegerType();
        } else if (dto instanceof FloatTypeDTO) {
            return new FloatType();
        } else if (dto instanceof StringTypeDTO) {
            return new StringType();
        } else if (dto instanceof BooleanTypeDTO) {
            return new BooleanType();
        } else if (dto instanceof DateTypeDTO) {
            return new DateType();
        } else if (dto instanceof EnumTypeDTO) {
            return new EnumType(((EnumTypeDTO) dto).getValues());
        }
        return null;
    }

    // Conversion Constraint
    private ConstraintDTO toConstraintDTO(BaseConstraint constraint) {
        if (constraint == null) return null;

        if (constraint instanceof MinMaxConstraint) {
            MinMaxConstraintDTO dto = new MinMaxConstraintDTO();
            dto.setMin(((MinMaxConstraint) constraint).getMin());
            dto.setMax(((MinMaxConstraint) constraint).getMax());
            return dto;
        } else if (constraint instanceof EnumConstraint) {
            EnumConstraintDTO dto = new EnumConstraintDTO();
            dto.setAllowed(((EnumConstraint) constraint).getAllowed());
            return dto;
        }
        return null;
    }

    private BaseConstraint toConstraint(ConstraintDTO dto) {
        if (dto == null) return null;

        if (dto instanceof MinMaxConstraintDTO) {
            return new MinMaxConstraint(
                    ((MinMaxConstraintDTO) dto).getMin(),
                    ((MinMaxConstraintDTO) dto).getMax()
            );
        } else if (dto instanceof EnumConstraintDTO) {
            return new EnumConstraint(((EnumConstraintDTO) dto).getAllowed());
        }
        return null;
    }

    // Conversion Generator
    private GeneratorDTO toGeneratorDTO(Object generator) {
        if (generator == null) return null;

        if (generator instanceof RandomDataGenerator) {
            return new RandomGeneratorDTO();
        } else if (generator instanceof SequentialDataGenerator) {
            SequentialGeneratorDTO dto = new SequentialGeneratorDTO();
            dto.setStartValue(((SequentialDataGenerator) generator).getStartValue());
            return dto;
        } else if (generator instanceof EmailDataGenerator) {
            return new EmailGeneratorDTO();
        }
        return null;
    }

    private com.example.microservice.generation.BaseDataGenerator toGenerator(GeneratorDTO dto) {
        if (dto == null) return null;

        if (dto instanceof RandomGeneratorDTO) {
            return new RandomDataGenerator();
        } else if (dto instanceof SequentialGeneratorDTO) {
            return new SequentialDataGenerator(((SequentialGeneratorDTO) dto).getStartValue());
        } else if (dto instanceof EmailGeneratorDTO) {
            return new EmailDataGenerator();
        }
        return null;
    }
}
