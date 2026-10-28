package com.example.generator_service.generation;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.generator_service.dataset.Dataset;
import com.example.generator_service.dataset.Record;
import com.example.generator_service.dataset.TableSchema;
import com.example.generator_service.dataset.TableSchema.Column;
import com.example.generator_service.dataset.TableSchema.ForeignKey;
import com.example.generator_service.dataset.TableSchema.SqlType;
import com.example.generator_service.dto.entity.AttributeDTO;
import com.example.generator_service.dto.entity.DataEntityResponseDTO;
import com.example.generator_service.dto.model.types.IntegerTypeDTO;
import com.example.generator_service.dto.model.types.StringTypeDTO;
import java.util.List;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

class RelationalDatasetBuilderTest {

    /** Name-based generators may ignore the declared type: "phone" is declared an integer but comes back as text. */
    private final RelationalDatasetBuilder builder = new RelationalDatasetBuilder(
            attribute -> switch (attribute.getName()) {
                case "phone" -> "06 12 34 56 78";
                case "total" -> 12.5;
                default -> 7;
            },
            new SplittableRandom(42));

    private final DataEntityResponseDTO shop = entity("customer", 20,
            List.of(attribute("id"), attribute("phone")),
            entity("order", 50, List.of(attribute("total"), attribute("customer_id")),
                    entity("line", 120, List.of(attribute("qty")))));

    @Test
    void everyChildRowReferencesAnExistingParentRow() {
        Dataset dataset = builder.build(List.of(shop));

        assertThat(dataset.getRecords("customer")).hasSize(20);
        assertThat(dataset.getRecords("order")).hasSize(50)
                .allSatisfy(order -> assertThat((Long) order.getValue("customer_id")).isBetween(1L, 20L));
        assertThat(dataset.getRecords("line")).hasSize(120)
                .allSatisfy(line -> assertThat((Long) line.getValue("order_id")).isBetween(1L, 50L));
    }

    @Test
    void keysAreTheGeneratorsEvenWhenAnAttributeHasTheSameName() {
        Dataset dataset = builder.build(List.of(shop));

        assertThat(dataset.getRecords("customer")).extracting(record -> record.getValue("id"))
                .containsExactlyElementsOf(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
                        11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L));
        // "customer_id" declared on order is the foreign key, not a generated value.
        assertThat(dataset.getRecords("order")).map(record -> record.getValue("customer_id"))
                .allSatisfy(value -> assertThat(value).isInstanceOf(Long.class));
    }

    @Test
    void parentsComeBeforeTheirSubEntities() {
        Dataset dataset = builder.build(List.of(shop));

        assertThat(dataset.getSchemas()).extracting(TableSchema::name).containsExactly("customer", "order", "line");
        assertThat(dataset.getSchemas().get(2).foreignKey()).isEqualTo(new ForeignKey("order_id", "order"));
    }

    @Test
    void columnTypesFollowTheGeneratedValues() {
        TableSchema customer = builder.build(List.of(shop)).getSchemas().getFirst();

        assertThat(customer.columns()).containsExactly(
                new Column("id", SqlType.BIGINT),
                new Column("phone", SqlType.TEXT));
    }

    @Test
    void aPreviewKeepsAFewRowsPerTableAndItsForeignKeysPointInsideThem() {
        Dataset preview = builder.preview(List.of(shop), 10);

        assertThat(preview.getRecords("customer")).hasSize(10);
        assertThat(preview.getRecords("order")).hasSize(10)
                .allSatisfy(order -> assertThat((Long) order.getValue("customer_id")).isBetween(1L, 10L));
        assertThat(preview.getRecords("line")).hasSize(10);
    }

    @Test
    void mixedIntegersAndDecimalsWidenToDouble() {
        Record whole = new Record();
        whole.setValue("price", 3);
        Record decimal = new Record();
        decimal.setValue("price", 3.5);
        Record missing = new Record();
        missing.setValue("price", null);

        TableSchema schema = RelationalDatasetBuilder.schemaOf("t", List.of(whole, decimal, missing), null);

        assertThat(schema.columns()).containsExactly(new Column("price", SqlType.DOUBLE));
    }

    static DataEntityResponseDTO entity(String name, int quantity, List<AttributeDTO> attributes,
                                        DataEntityResponseDTO... children) {
        return DataEntityResponseDTO.builder().name(name).quantity(quantity).attributes(attributes)
                .subEntities(List.of(children)).build();
    }

    static AttributeDTO attribute(String name) {
        return AttributeDTO.builder().name(name).type(name.equals("phone") ? new IntegerTypeDTO() : new StringTypeDTO())
                .build();
    }
}
