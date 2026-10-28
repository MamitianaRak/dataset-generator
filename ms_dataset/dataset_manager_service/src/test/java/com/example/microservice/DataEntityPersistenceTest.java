package com.example.microservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.microservice.dto.entity.AttributeDTO;
import com.example.microservice.dto.entity.DataEntityCreateDTO;
import com.example.microservice.dto.entity.DataEntityResponseDTO;
import com.example.microservice.dto.entity.DataEntityUpdateDTO;
import com.example.microservice.dto.model.constraints.MinMaxConstraintDTO;
import com.example.microservice.dto.model.types.EnumTypeDTO;
import com.example.microservice.dto.model.types.IntegerTypeDTO;
import com.example.microservice.dto.model.types.StringTypeDTO;
import com.example.microservice.dto.project.ProjectCreateDTO;
import com.example.microservice.model.entities.DataEntity;
import com.example.microservice.model.entities.Project;
import com.example.microservice.repository.DataEntityRepository;
import com.example.microservice.service.DataEntityService;
import com.example.microservice.service.ProjectService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

@SpringBootTest(properties = {"spring.cloud.config.enabled=false", "eureka.client.enabled=false"})
class DataEntityPersistenceTest {

    @Autowired
    private ProjectService projects;
    @Autowired
    private DataEntityService entities;
    @Autowired
    private DataEntityRepository repository;

    @Test
    void typesAndConstraintsSurviveTheRoundTripThroughTheJsonColumn() {
        Long projectId = project("round-trip");
        EnumTypeDTO colour = new EnumTypeDTO();
        colour.setValues(List.of("red", "green"));
        AttributeDTO age = attribute("age", new IntegerTypeDTO());
        age.setConstraints(List.of(range(18, 99)));

        DataEntityResponseDTO created = entities.createEntity(projectId,
                entity("person", List.of(age, attribute("colour", colour))));
        DataEntityResponseDTO reloaded = entities.findById(created.getId());

        Map<String, AttributeDTO> byName = byName(reloaded);
        assertThat(byName.get("age").getType()).isInstanceOf(IntegerTypeDTO.class);
        assertThat(byName.get("age").getConstraints()).singleElement()
                .isInstanceOfSatisfying(MinMaxConstraintDTO.class, c -> {
                    assertThat(c.getMin()).isEqualTo(18);
                    assertThat(c.getMax()).isEqualTo(99);
                });
        assertThat(byName.get("colour").getType()).isInstanceOfSatisfying(EnumTypeDTO.class,
                t -> assertThat(t.getValues()).containsExactly("red", "green"));
    }

    @Test
    void anUpdateKeepsUnchangedAttributesInPlaceInsteadOfRecreatingThem() {
        Long projectId = project("update");
        DataEntityResponseDTO created = entities.createEntity(projectId, entity("customer",
                List.of(attribute("name", new StringTypeDTO()), attribute("age", new IntegerTypeDTO()))));
        Long nameId = byName(created).get("name").getId();

        AttributeDTO age = attribute("age", new IntegerTypeDTO());
        age.setConstraints(List.of(range(0, 120)));
        DataEntityUpdateDTO update = new DataEntityUpdateDTO("customer", 50,
                List.of(attribute("name", new StringTypeDTO()), age, attribute("email", new StringTypeDTO())));
        DataEntityResponseDTO updated = entities.updateEntity(created.getId(), update);

        Map<String, AttributeDTO> byName = byName(updated);
        assertThat(byName).containsOnlyKeys("name", "age", "email");
        assertThat(byName.get("name").getId()).isEqualTo(nameId);
        assertThat(byName.get("age").getConstraints()).hasSize(1);
        assertThat(updated.getQuantity()).isEqualTo(50);

        DataEntityUpdateDTO shrink = new DataEntityUpdateDTO("customer", 50, List.of(attribute("email", new StringTypeDTO())));
        assertThat(byName(entities.updateEntity(created.getId(), shrink))).containsOnlyKeys("email");
    }

    @Test
    void subEntitiesBelongToTheProjectAndShareItsNameSpace() {
        Long projectId = project("names");
        DataEntityCreateDTO order = entity("order", List.of(attribute("total", new IntegerTypeDTO())));
        order.setSubEntities(List.of(entity("line", List.of(attribute("qty", new IntegerTypeDTO())))));
        DataEntityResponseDTO created = entities.createEntity(projectId, order);

        assertThat(created.getSubEntities()).singleElement()
                .satisfies(line -> assertThat(line.getProjectId()).isEqualTo(projectId));
        // "line" becomes a table name in the SQL export: a second one in the project would collide.
        assertThatThrownBy(() -> entities.createEntity(projectId, entity("line", List.of())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aSubEntityCanBeAddedToAnExistingEntity() {
        Long projectId = project("add-child");
        DataEntityResponseDTO customer = entities.createEntity(projectId,
                entity("customer", List.of(attribute("name", new StringTypeDTO()))));

        DataEntityResponseDTO order = entities.createSubEntity(customer.getId(),
                entity("order", List.of(attribute("total", new IntegerTypeDTO()))));

        assertThat(order.getProjectId()).isEqualTo(projectId);
        assertThat(entities.findEntitiesByProject(projectId)).singleElement()
                .satisfies(root -> assertThat(root.getSubEntities()).extracting(DataEntityResponseDTO::getName)
                        .containsExactly("order"));
        assertThatThrownBy(() -> entities.createSubEntity(customer.getId(), entity("customer", List.of())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void duplicateAttributeNamesAreRejected() {
        Long projectId = project("duplicates");
        DataEntityCreateDTO entity = entity("t", List.of(
                attribute("a", new StringTypeDTO()), attribute("a", new IntegerTypeDTO())));

        assertThatThrownBy(() -> entities.createEntity(projectId, entity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate attribute name: a");
    }

    @Test
    void deletingAProjectRemovesItsEntitiesThroughTheDatabaseCascade() {
        Long projectId = project("cascade");
        DataEntityCreateDTO parent = entity("parent", List.of(attribute("x", new IntegerTypeDTO())));
        parent.setSubEntities(List.of(entity("child", List.of(attribute("y", new IntegerTypeDTO())))));
        entities.createEntity(projectId, parent);

        projects.delete(projectId);

        assertThat(repository.findAllWithAttributesByProjectId(projectId)).isEmpty();
    }

    @Test
    void aStaleWriteIsRefusedByTheOptimisticLock() {
        Long projectId = project("locking");
        Long id = entities.createEntity(projectId, entity("t", List.of())).getId();
        DataEntity stale = repository.findById(id).orElseThrow(); // detached: read outside any transaction

        entities.updateEntity(id, new DataEntityUpdateDTO("t", 99, List.of()));
        stale.setQuantity(1);

        assertThatThrownBy(() -> repository.save(stale)).isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void unsavedEntitiesAreNotEqualToEachOther() {
        assertThat(new Project("a")).isNotEqualTo(new Project("a"));
    }

    private Long project(String name) {
        return projects.save(new ProjectCreateDTO(name)).getId();
    }

    private static DataEntityCreateDTO entity(String name, List<AttributeDTO> attributes) {
        DataEntityCreateDTO entity = new DataEntityCreateDTO();
        entity.setName(name);
        entity.setQuantity(10);
        entity.setAttributes(attributes);
        return entity;
    }

    private static AttributeDTO attribute(String name, com.example.microservice.dto.model.types.DataTypeDTO type) {
        AttributeDTO attribute = new AttributeDTO();
        attribute.setName(name);
        attribute.setType(type);
        return attribute;
    }

    private static MinMaxConstraintDTO range(double min, double max) {
        MinMaxConstraintDTO range = new MinMaxConstraintDTO();
        range.setMin(min);
        range.setMax(max);
        return range;
    }

    private static Map<String, AttributeDTO> byName(DataEntityResponseDTO entity) {
        return entity.getAttributes().stream().collect(Collectors.toMap(AttributeDTO::getName, Function.identity()));
    }
}
