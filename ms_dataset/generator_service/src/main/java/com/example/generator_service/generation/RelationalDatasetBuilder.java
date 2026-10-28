package com.example.generator_service.generation;

import com.example.generator_service.dataset.Dataset;
import com.example.generator_service.dataset.Record;
import com.example.generator_service.dataset.RowSink;
import com.example.generator_service.dataset.TableSchema;
import com.example.generator_service.dataset.TableSchema.Column;
import com.example.generator_service.dataset.TableSchema.ForeignKey;
import com.example.generator_service.dataset.TableSchema.SqlType;
import com.example.generator_service.dto.entity.AttributeDTO;
import com.example.generator_service.dto.entity.DataEntityResponseDTO;
import com.example.generator_service.generation.interfaces.IDataGenerator;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * Generates a project's tables as a relational dataset. Each table gets an {@code id} key (1..n); a sub-entity's
 * table gets a {@code <parent>_id} column holding the id of an existing row of its parent, so the SQL export loads
 * into a database with its foreign keys enforced. Parents are generated first: their ids exist when the children
 * pick one. Only parent row counts are kept, never the rows: generation streams to a {@link RowSink}.
 */
public class RelationalDatasetBuilder {

    /**
     * Rows generated before a table's column types are decided. A value generator returns one Java type per
     * attribute, so the sample settles the types; the rest of the table is then streamed without being kept.
     */
    static final int TYPE_SAMPLE = 1_000;

    private final IDataGenerator values;
    private final RandomGenerator random;

    public RelationalDatasetBuilder(IDataGenerator values, RandomGenerator random) {
        this.values = values;
        this.random = random;
    }

    /** Every row, in memory: for tests and small datasets. */
    public Dataset build(List<DataEntityResponseDTO> roots) {
        return preview(roots, Integer.MAX_VALUE);
    }

    /** At most {@code maxRowsPerTable} rows per table, foreign keys pointing inside the rows kept. */
    public Dataset preview(List<DataEntityResponseDTO> roots, int maxRowsPerTable) {
        Dataset dataset = new Dataset();
        generate(roots, maxRowsPerTable, dataset);
        return dataset;
    }

    /** Streams every table to {@code sink}, parents first. */
    public void generate(List<DataEntityResponseDTO> roots, int maxRowsPerTable, RowSink sink) {
        for (DataEntityResponseDTO root : roots) {
            if (root != null) {
                generate(root, null, 0, maxRowsPerTable, sink);
            }
        }
    }

    private void generate(DataEntityResponseDTO entity, String parentTable, int parentRows, int maxRows, RowSink sink) {
        int rows = Math.min(Math.max(0, entity.getQuantity()), maxRows);
        if (rows == 0) {
            return; // nothing to generate, and nothing for its sub-entities to reference
        }
        ForeignKey foreignKey = parentTable == null ? null : new ForeignKey(parentTable + "_id", parentTable);

        List<Record> sample = new ArrayList<>(Math.min(rows, TYPE_SAMPLE));
        for (long id = 1; id <= Math.min(rows, TYPE_SAMPLE); id++) {
            sample.add(record(entity, id, foreignKey, parentRows));
        }
        sink.table(schemaOf(entity.getName(), sample, foreignKey));
        sample.forEach(sink::row);
        for (long id = sample.size() + 1L; id <= rows; id++) {
            sink.row(record(entity, id, foreignKey, parentRows));
        }

        if (entity.getSubEntities() != null) {
            for (DataEntityResponseDTO child : entity.getSubEntities()) {
                generate(child, entity.getName(), rows, maxRows, sink);
            }
        }
    }

    private Record record(DataEntityResponseDTO entity, long id, ForeignKey foreignKey, int parentRows) {
        Record record = new Record();
        record.setValue(TableSchema.PRIMARY_KEY, id);
        if (foreignKey != null) {
            record.setValue(foreignKey.column(), 1L + random.nextInt(parentRows));
        }
        for (AttributeDTO attribute : attributes(entity)) {
            // The key columns are the generator's: an attribute with the same name would break the relations.
            if (!isKeyColumn(attribute.getName(), foreignKey)) {
                record.setValue(attribute.getName(), values.generateFromDTO(attribute));
            }
        }
        return record;
    }

    private static List<AttributeDTO> attributes(DataEntityResponseDTO entity) {
        return entity.getAttributes() == null ? List.of() : entity.getAttributes();
    }

    private static boolean isKeyColumn(String name, ForeignKey foreignKey) {
        return TableSchema.PRIMARY_KEY.equalsIgnoreCase(name)
                || foreignKey != null && foreignKey.column().equalsIgnoreCase(name);
    }

    /** Column types from the values generated: what was declared may differ from what a name-based generator returns. */
    static TableSchema schemaOf(String table, List<Record> records, ForeignKey foreignKey) {
        Map<String, SqlType> types = new LinkedHashMap<>();
        for (Record record : records) {
            // Not Map.merge: a column may hold only nulls so far, and merge rejects null values.
            record.getValues().forEach((column, value) -> types.put(column, widen(types.get(column), typeOf(value))));
        }
        List<Column> columns = types.entrySet().stream()
                .map(e -> new Column(e.getKey(), e.getValue() == null ? SqlType.TEXT : e.getValue()))
                .toList();
        return new TableSchema(table, columns, foreignKey);
    }

    private static SqlType typeOf(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer || value instanceof Long || value instanceof Short) {
            return SqlType.BIGINT;
        }
        if (value instanceof Number) {
            return SqlType.DOUBLE;
        }
        if (value instanceof Boolean) {
            return SqlType.BOOLEAN;
        }
        if (value instanceof Date || value instanceof Temporal) {
            return SqlType.TIMESTAMP;
        }
        return SqlType.TEXT;
    }

    /** The type that holds both: integers widen to doubles, anything else mixed falls back to text. */
    private static SqlType widen(SqlType a, SqlType b) {
        if (a == null) {
            return b;
        }
        if (b == null || a == b) {
            return a;
        }
        boolean numeric = (a == SqlType.BIGINT || a == SqlType.DOUBLE) && (b == SqlType.BIGINT || b == SqlType.DOUBLE);
        return numeric ? SqlType.DOUBLE : SqlType.TEXT;
    }
}
