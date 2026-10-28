package com.example.microservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.microservice.dto.entity.AttributeDTO;
import com.example.microservice.dto.entity.DataEntityCreateDTO;
import com.example.microservice.dto.entity.DataEntityResponseDTO;
import com.example.microservice.dto.model.constraints.EnumConstraintDTO;
import com.example.microservice.dto.model.constraints.MinMaxConstraintDTO;
import com.example.microservice.dto.model.types.DataTypeDTO;
import com.example.microservice.dto.model.types.EnumTypeDTO;
import com.example.microservice.dto.model.types.FloatTypeDTO;
import com.example.microservice.dto.model.types.IntegerTypeDTO;
import com.example.microservice.dto.model.types.StringTypeDTO;
import com.example.microservice.dto.project.ProjectCreateDTO;
import com.example.microservice.service.DataEntityService;
import com.example.microservice.service.ProjectService;
import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Counts the SQL statements needed to load a project's entities, the call the generator service makes before
 * every preview and export. A realistic project: 5 entities of 6 attributes, each with one sub-entity of 3.
 */
@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.jpa.properties.hibernate.generate_statistics=true",
})
class ProjectLoadingQueryCountTest {

    @Autowired
    private ProjectService projects;
    @Autowired
    private DataEntityService entities;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void loadingAProjectTakesAFewQueries() {
        Long projectId = projects.save(new ProjectCreateDTO("shop")).getId();
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();

        statistics.clear();
        for (int i = 0; i < 5; i++) {
            DataEntityCreateDTO entity = entity("table" + i, attributes(6));
            entity.setSubEntities(List.of(entity("child" + i, attributes(3))));
            entities.createEntity(projectId, entity);
        }
        long creation = statistics.getPrepareStatementCount();

        statistics.clear();
        List<DataEntityResponseDTO> loaded = entities.findEntitiesByProject(projectId);
        long loading = statistics.getPrepareStatementCount();

        System.out.printf("%nProject of 5 entities x 6 attributes (+ 5 sub-entities x 3): "
                + "creation %d SQL statements, loading %d SQL statements%n%n", creation, loading);
        // Before the rework: 210 statements to create, 87 to load (an N+1 per entity, attribute and type).
        // Loading is now the project check plus one query; any new lazy access in the mapping would break this.
        assertThat(loading).isEqualTo(2);
        assertThat(creation).isLessThanOrEqualTo(30);
        assertThat(loaded).hasSize(5);
        assertThat(loaded.getFirst().getAttributes()).hasSize(6);
        assertThat(loaded.getFirst().getSubEntities().getFirst().getAttributes()).hasSize(3);
    }

    private static DataEntityCreateDTO entity(String name, List<AttributeDTO> attributes) {
        DataEntityCreateDTO entity = new DataEntityCreateDTO();
        entity.setName(name);
        entity.setQuantity(10);
        entity.setAttributes(attributes);
        return entity;
    }

    /** A mix of types and constraints, as a real table would have. */
    private static List<AttributeDTO> attributes(int count) {
        List<AttributeDTO> attributes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            AttributeDTO attribute = new AttributeDTO();
            attribute.setName("field" + i);
            attribute.setType(type(i));
            if (i % 3 == 1) {
                MinMaxConstraintDTO range = new MinMaxConstraintDTO();
                range.setMin(0);
                range.setMax(100);
                attribute.setConstraints(List.of(range));
            } else if (i % 3 == 2) {
                EnumConstraintDTO allowed = new EnumConstraintDTO();
                allowed.setAllowed(List.of("A", "B"));
                attribute.setConstraints(List.of(allowed));
            }
            attributes.add(attribute);
        }
        return attributes;
    }

    private static DataTypeDTO type(int i) {
        return switch (i % 4) {
            case 0 -> new StringTypeDTO();
            case 1 -> new IntegerTypeDTO();
            case 2 -> new FloatTypeDTO();
            default -> {
                EnumTypeDTO type = new EnumTypeDTO();
                type.setValues(List.of("red", "green", "blue"));
                yield type;
            }
        };
    }
}
