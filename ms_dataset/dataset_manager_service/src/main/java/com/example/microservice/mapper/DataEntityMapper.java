package com.example.microservice.mapper;

import com.example.microservice.dto.entity.AttributeDTO;
import com.example.microservice.dto.entity.DataEntityCreateDTO;
import com.example.microservice.dto.entity.DataEntityResponseDTO;
import com.example.microservice.dto.entity.DataEntityUpdateDTO;
import com.example.microservice.dto.model.constraints.ConstraintDTO;
import com.example.microservice.dto.model.constraints.EnumConstraintDTO;
import com.example.microservice.dto.model.constraints.MinMaxConstraintDTO;
import com.example.microservice.dto.model.generators.FakerGeneratorDTO;
import com.example.microservice.dto.model.generators.GeneratorDTO;
import com.example.microservice.dto.model.generators.SequentialGeneratorDTO;
import com.example.microservice.dto.model.types.*;
import com.example.microservice.generation.BaseDataGenerator;
import com.example.microservice.generation.FakerDataGenerator;
import com.example.microservice.generation.SequentialDataGenerator;
import com.example.microservice.model.constraints.BaseConstraint;
import com.example.microservice.model.constraints.EnumConstraint;
import com.example.microservice.model.constraints.MinMaxConstraint;
import com.example.microservice.model.entities.Attribute;
import com.example.microservice.model.entities.DataEntity;
import com.example.microservice.model.types.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mapper pour DataEntity et Attribute.
 * Gère la conversion entre DTOs et entités avec polymorphie.
 */
@Component
public class DataEntityMapper {

    // Registres Entity → DTO
    private static final Map<Class<? extends BaseDataType>, Function<BaseDataType, DataTypeDTO>> TYPE_TO_DTO = new LinkedHashMap<>();
    private static final Map<Class<? extends BaseConstraint>, Function<BaseConstraint, ConstraintDTO>> CONSTRAINT_TO_DTO = new LinkedHashMap<>();
    private static final Map<Class<? extends BaseDataGenerator>, Function<BaseDataGenerator, GeneratorDTO>> GENERATOR_TO_DTO = new LinkedHashMap<>();

    // Registres DTO → Entity
    private static final Map<Class<? extends DataTypeDTO>, Function<DataTypeDTO, BaseDataType>> DTO_TO_TYPE = new LinkedHashMap<>();
    private static final Map<Class<? extends ConstraintDTO>, Function<ConstraintDTO, BaseConstraint>> DTO_TO_CONSTRAINT = new LinkedHashMap<>();
    private static final Map<Class<? extends GeneratorDTO>, Function<GeneratorDTO, BaseDataGenerator>> DTO_TO_GENERATOR = new LinkedHashMap<>();

    static {
        // Type Entity → DTO
        TYPE_TO_DTO.put(IntegerType.class, t -> new IntegerTypeDTO());
        TYPE_TO_DTO.put(FloatType.class, t -> new FloatTypeDTO());
        TYPE_TO_DTO.put(StringType.class, t -> new StringTypeDTO());
        TYPE_TO_DTO.put(BooleanType.class, t -> new BooleanTypeDTO());
        TYPE_TO_DTO.put(DateType.class, t -> new DateTypeDTO());
        TYPE_TO_DTO.put(EnumType.class, t -> {
            EnumTypeDTO dto = new EnumTypeDTO();
            dto.setValues(((EnumType) t).getValues());
            return dto;
        });

        // Type DTO → Entity
        DTO_TO_TYPE.put(IntegerTypeDTO.class, d -> new IntegerType());
        DTO_TO_TYPE.put(FloatTypeDTO.class, d -> new FloatType());
        DTO_TO_TYPE.put(StringTypeDTO.class, d -> new StringType());
        DTO_TO_TYPE.put(BooleanTypeDTO.class, d -> new BooleanType());
        DTO_TO_TYPE.put(DateTypeDTO.class, d -> new DateType());
        DTO_TO_TYPE.put(EnumTypeDTO.class, d -> new EnumType(((EnumTypeDTO) d).getValues()));

        // Constraint Entity → DTO
        CONSTRAINT_TO_DTO.put(MinMaxConstraint.class, c -> {
            MinMaxConstraintDTO dto = new MinMaxConstraintDTO();
            dto.setMin(((MinMaxConstraint) c).getMin());
            dto.setMax(((MinMaxConstraint) c).getMax());
            return dto;
        });
        CONSTRAINT_TO_DTO.put(EnumConstraint.class, c -> {
            EnumConstraintDTO dto = new EnumConstraintDTO();
            dto.setAllowed(((EnumConstraint) c).getAllowed());
            return dto;
        });

        // Constraint DTO → Entity
        DTO_TO_CONSTRAINT.put(MinMaxConstraintDTO.class, d -> new MinMaxConstraint(
                ((MinMaxConstraintDTO) d).getMin(), ((MinMaxConstraintDTO) d).getMax()));
        DTO_TO_CONSTRAINT.put(EnumConstraintDTO.class, d -> new EnumConstraint(((EnumConstraintDTO) d).getAllowed()));

        // Generator Entity → DTO
        GENERATOR_TO_DTO.put(FakerDataGenerator.class, g -> {
            FakerGeneratorDTO dto = new FakerGeneratorDTO();
            dto.setLocale(((FakerDataGenerator) g).getFakerLocale());
            return dto;
        });
        GENERATOR_TO_DTO.put(SequentialDataGenerator.class, g -> {
            SequentialGeneratorDTO dto = new SequentialGeneratorDTO();
            dto.setStartValue(((SequentialDataGenerator) g).getStartValue());
            return dto;
        });

        // Generator DTO → Entity
        DTO_TO_GENERATOR.put(FakerGeneratorDTO.class, d -> new FakerDataGenerator(((FakerGeneratorDTO) d).getLocale()));
        DTO_TO_GENERATOR.put(SequentialGeneratorDTO.class, d -> new SequentialDataGenerator(((SequentialGeneratorDTO) d).getStartValue()));
    }

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

        // Note: attributes are cleared in the service before calling this method
        if (dto.getAttributes() != null) {
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

    private DataTypeDTO toDataTypeDTO(BaseDataType type) {
        if (type == null) return null;
        Function<BaseDataType, DataTypeDTO> converter = TYPE_TO_DTO.get(type.getClass());
        return converter != null ? converter.apply(type) : null;
    }

    private BaseDataType toDataType(DataTypeDTO dto) {
        if (dto == null) return null;
        Function<DataTypeDTO, BaseDataType> converter = DTO_TO_TYPE.get(dto.getClass());
        return converter != null ? converter.apply(dto) : null;
    }

    private ConstraintDTO toConstraintDTO(BaseConstraint constraint) {
        if (constraint == null) return null;
        Function<BaseConstraint, ConstraintDTO> converter = CONSTRAINT_TO_DTO.get(constraint.getClass());
        return converter != null ? converter.apply(constraint) : null;
    }

    private BaseConstraint toConstraint(ConstraintDTO dto) {
        if (dto == null) return null;
        Function<ConstraintDTO, BaseConstraint> converter = DTO_TO_CONSTRAINT.get(dto.getClass());
        return converter != null ? converter.apply(dto) : null;
    }

    private GeneratorDTO toGeneratorDTO(BaseDataGenerator generator) {
        if (generator == null) return null;
        Function<BaseDataGenerator, GeneratorDTO> converter = GENERATOR_TO_DTO.get(generator.getClass());
        return converter != null ? converter.apply(generator) : null;
    }

    private BaseDataGenerator toGenerator(GeneratorDTO dto) {
        if (dto == null) return null;
        Function<GeneratorDTO, BaseDataGenerator> converter = DTO_TO_GENERATOR.get(dto.getClass());
        return converter != null ? converter.apply(dto) : null;
    }
}
