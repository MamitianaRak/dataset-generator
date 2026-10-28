package com.example.microservice.mapper;

import com.example.microservice.dto.entity.AttributeDTO;
import com.example.microservice.dto.entity.DataEntityResponseDTO;
import com.example.microservice.dto.model.constraints.ConstraintDTO;
import com.example.microservice.dto.model.constraints.EnumConstraintDTO;
import com.example.microservice.dto.model.constraints.MinMaxConstraintDTO;
import com.example.microservice.dto.model.types.BooleanTypeDTO;
import com.example.microservice.dto.model.types.DataTypeDTO;
import com.example.microservice.dto.model.types.DateTypeDTO;
import com.example.microservice.dto.model.types.EnumTypeDTO;
import com.example.microservice.dto.model.types.FloatTypeDTO;
import com.example.microservice.dto.model.types.IntegerTypeDTO;
import com.example.microservice.dto.model.types.StringTypeDTO;
import com.example.microservice.model.entities.Attribute;
import com.example.microservice.model.entities.DataEntity;
import com.example.microservice.model.entities.DataEntity.AttributeSpec;
import com.example.microservice.model.schema.AttributeDefinition;
import com.example.microservice.model.schema.ValueConstraint;
import com.example.microservice.model.schema.ValueType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;

/**
 * Converts between the API's DTOs (kept unchanged for the front end and the generator service) and the model.
 * An unknown type or a duplicate attribute name is rejected instead of being silently dropped.
 */
@Component
public class DataEntityMapper {

    /** Attribute specs keyed by name, in request order. */
    public Map<String, AttributeSpec> toSpecs(List<AttributeDTO> attributes) {
        Map<String, AttributeSpec> specs = new LinkedHashMap<>();
        if (attributes == null) {
            return specs;
        }
        for (AttributeDTO dto : attributes) {
            AttributeSpec spec = new AttributeSpec(dto.getQuantity(), toDefinition(dto));
            if (specs.putIfAbsent(dto.getName(), spec) != null) {
                throw new IllegalArgumentException("Duplicate attribute name: " + dto.getName());
            }
        }
        return specs;
    }

    /**
     * Rebuilds the trees of the given entities: each root selected by {@code isRoot} with its sub-entities, found
     * among {@code all} by parent id. No lazy collection is touched, so no extra query is issued.
     */
    public List<DataEntityResponseDTO> toTrees(List<DataEntity> all, Predicate<DataEntity> isRoot) {
        Map<Long, List<DataEntity>> childrenByParent = new HashMap<>();
        for (DataEntity entity : all) {
            if (entity.getParentId() != null) {
                childrenByParent.computeIfAbsent(entity.getParentId(), id -> new ArrayList<>()).add(entity);
            }
        }
        return all.stream().filter(isRoot).map(root -> toResponse(root, childrenByParent)).toList();
    }

    private DataEntityResponseDTO toResponse(DataEntity entity, Map<Long, List<DataEntity>> childrenByParent) {
        return DataEntityResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .quantity(entity.getQuantity())
                .projectId(entity.getProject().getId())
                .attributes(entity.getAttributes().stream()
                        .sorted(Comparator.comparing(Attribute::getId))
                        .map(this::toAttributeDTO)
                        .toList())
                .subEntities(childrenByParent.getOrDefault(entity.getId(), List.of()).stream()
                        .map(child -> toResponse(child, childrenByParent))
                        .toList())
                .build();
    }

    private AttributeDTO toAttributeDTO(Attribute attribute) {
        AttributeDefinition definition = attribute.getDefinition();
        return AttributeDTO.builder()
                .id(attribute.getId())
                .name(attribute.getName())
                .quantity(attribute.getQuantity())
                .type(toTypeDTO(definition.type()))
                .constraints(definition.constraints().stream().map(DataEntityMapper::toConstraintDTO).toList())
                .build();
    }

    private static AttributeDefinition toDefinition(AttributeDTO dto) {
        List<ValueConstraint> constraints = dto.getConstraints() == null ? List.of()
                : dto.getConstraints().stream().map(DataEntityMapper::toConstraint).toList();
        return new AttributeDefinition(toType(dto.getType()), constraints);
    }

    private static ValueType toType(DataTypeDTO dto) {
        return switch (dto) {
            case IntegerTypeDTO ignored -> new ValueType.IntegerValue();
            case FloatTypeDTO ignored -> new ValueType.FloatValue();
            case StringTypeDTO ignored -> new ValueType.StringValue();
            case BooleanTypeDTO ignored -> new ValueType.BooleanValue();
            case DateTypeDTO ignored -> new ValueType.DateValue();
            case EnumTypeDTO type -> new ValueType.EnumValue(type.getValues());
            case null -> throw new IllegalArgumentException("Attribute type is required");
            default -> throw new IllegalArgumentException("Unsupported type: " + dto.getClass().getSimpleName());
        };
    }

    private static DataTypeDTO toTypeDTO(ValueType type) {
        return switch (type) {
            case ValueType.IntegerValue ignored -> new IntegerTypeDTO();
            case ValueType.FloatValue ignored -> new FloatTypeDTO();
            case ValueType.StringValue ignored -> new StringTypeDTO();
            case ValueType.BooleanValue ignored -> new BooleanTypeDTO();
            case ValueType.DateValue ignored -> new DateTypeDTO();
            case ValueType.EnumValue enumValue -> {
                EnumTypeDTO dto = new EnumTypeDTO();
                dto.setValues(enumValue.values());
                yield dto;
            }
        };
    }

    private static ValueConstraint toConstraint(ConstraintDTO dto) {
        return switch (dto) {
            case MinMaxConstraintDTO range -> new ValueConstraint.Range(range.getMin(), range.getMax());
            case EnumConstraintDTO allowed -> new ValueConstraint.AllowedValues(allowed.getAllowed());
            case null -> throw new IllegalArgumentException("Constraint is required");
            default -> throw new IllegalArgumentException("Unsupported constraint: " + dto.getClass().getSimpleName());
        };
    }

    private static ConstraintDTO toConstraintDTO(ValueConstraint constraint) {
        return switch (constraint) {
            case ValueConstraint.Range range -> {
                MinMaxConstraintDTO dto = new MinMaxConstraintDTO();
                dto.setMin(range.min());
                dto.setMax(range.max());
                yield dto;
            }
            case ValueConstraint.AllowedValues allowed -> {
                EnumConstraintDTO dto = new EnumConstraintDTO();
                dto.setAllowed(allowed.values());
                yield dto;
            }
        };
    }
}
