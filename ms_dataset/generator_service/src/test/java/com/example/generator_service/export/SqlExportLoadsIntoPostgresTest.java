package com.example.generator_service.export;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.generator_service.dataset.Dataset;
import com.example.generator_service.dto.entity.AttributeDTO;
import com.example.generator_service.dto.entity.DataEntityResponseDTO;
import com.example.generator_service.dto.model.constraints.MinMaxConstraintDTO;
import com.example.generator_service.dto.model.types.BooleanTypeDTO;
import com.example.generator_service.dto.model.types.DataTypeDTO;
import com.example.generator_service.dto.model.types.DateTypeDTO;
import com.example.generator_service.dto.model.types.EnumTypeDTO;
import com.example.generator_service.dto.model.types.FloatTypeDTO;
import com.example.generator_service.dto.model.types.IntegerTypeDTO;
import com.example.generator_service.dto.model.types.StringTypeDTO;
import com.example.generator_service.generation.FakerDataGenerator;
import com.example.generator_service.generation.RelationalDatasetBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * The promise of the SQL export: it loads as is into PostgreSQL, foreign keys enforced. Real Faker values, every
 * attribute type, and a table named "order" (a reserved word).
 */
@Testcontainers
class SqlExportLoadsIntoPostgresTest {

    @Container
    // A slow disk (Docker in WSL) can take over a minute to initialise the database: the default is 60 s.
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine")
            .withStartupTimeout(Duration.ofMinutes(3));

    @TempDir
    Path tempDir;

    @Test
    void theExportedScriptLoadsWithItsForeignKeysEnforced() throws Exception {
        DataEntityResponseDTO shop = entity("customer", 200, List.of(
                        attribute("lastName", new StringTypeDTO()),
                        attribute("email", new StringTypeDTO()),
                        attribute("vip", new BooleanTypeDTO()),
                        attribute("signedUp", new DateTypeDTO())),
                entity("order", 1_000, List.of(
                                attribute("total", new FloatTypeDTO()),
                                attribute("status", status())),
                        entity("line", 3_000, List.of(
                                quantity(),
                                attribute("label", new StringTypeDTO())))));
        Dataset dataset = new RelationalDatasetBuilder(new FakerDataGenerator("fr"), new SplittableRandom(7))
                .build(List.of(shop));
        Path script = tempDir.resolve("shop.sql");
        try (var out = Files.newBufferedWriter(script)) {
            new SQLExporter().export(dataset, out);
        }

        try (Connection db = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(),
                POSTGRES.getPassword());
             Statement sql = db.createStatement()) {
            sql.execute(Files.readString(script));

            assertThat(count(sql, "select count(*) from customer")).isEqualTo(200);
            assertThat(count(sql, "select count(*) from \"order\"")).isEqualTo(1_000);
            assertThat(count(sql, "select count(*) from line")).isEqualTo(3_000);
            assertThat(count(sql, """
                    select count(*) from information_schema.table_constraints
                    where constraint_type = 'FOREIGN KEY'""")).isEqualTo(2);
            // Joins work: every line reaches its customer through its order.
            assertThat(count(sql, """
                    select count(*) from line l
                    join "order" o on o.id = l.order_id
                    join customer c on c.id = o.customer_id""")).isEqualTo(3_000);
        }
    }

    private static long count(Statement sql, String query) throws Exception {
        try (ResultSet result = sql.executeQuery(query)) {
            result.next();
            return result.getLong(1);
        }
    }

    private static DataEntityResponseDTO entity(String name, int quantity, List<AttributeDTO> attributes,
                                                DataEntityResponseDTO... children) {
        return DataEntityResponseDTO.builder().name(name).quantity(quantity).attributes(attributes)
                .subEntities(List.of(children)).build();
    }

    private static AttributeDTO attribute(String name, DataTypeDTO type) {
        return AttributeDTO.builder().name(name).type(type).build();
    }

    private static AttributeDTO quantity() {
        MinMaxConstraintDTO range = new MinMaxConstraintDTO();
        range.setMin(1);
        range.setMax(10);
        return AttributeDTO.builder().name("quantity").type(new IntegerTypeDTO()).constraints(List.of(range)).build();
    }

    private static EnumTypeDTO status() {
        EnumTypeDTO status = new EnumTypeDTO();
        status.setValues(List.of("PAID", "SHIPPED", "CANCELLED"));
        return status;
    }
}
