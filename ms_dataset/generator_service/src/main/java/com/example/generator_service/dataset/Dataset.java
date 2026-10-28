package com.example.generator_service.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generated tables held in memory, with their schema: used for the preview, which is capped to a few rows per
 * table. Exports do not build one; they stream rows to a writer. Tables are kept in the order they were added,
 * parents before their sub-entities. Built by one request on one thread: no synchronisation needed.
 */
public class Dataset implements RowSink {

    private final Map<String, List<Record>> records = new LinkedHashMap<>();
    private final Map<String, TableSchema> schemas = new LinkedHashMap<>();
    private List<Record> current;

    @Override
    public void table(TableSchema schema) {
        if (schemas.putIfAbsent(schema.name(), schema) != null) {
            throw new IllegalArgumentException("Duplicate table name: " + schema.name());
        }
        current = new ArrayList<>();
        records.put(schema.name(), current);
    }

    @Override
    public void row(Record record) {
        if (current == null) {
            throw new IllegalStateException("row() before table()");
        }
        current.add(record);
    }

    /** Replays the tables, in order, into another sink. */
    public void replay(RowSink sink) {
        for (TableSchema schema : schemas.values()) {
            sink.table(schema);
            records.get(schema.name()).forEach(sink::row);
        }
    }

    public List<Record> getRecords(String entityName) {
        return Collections.unmodifiableList(records.getOrDefault(entityName, List.of()));
    }

    /** Table name to rows, parents first. */
    public Map<String, List<Record>> getAllRecords() {
        return Collections.unmodifiableMap(records);
    }

    /** Table schemas, parents first. */
    public List<TableSchema> getSchemas() {
        return List.copyOf(schemas.values());
    }
}
