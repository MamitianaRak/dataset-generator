package com.example.generator_service.export;

import com.example.generator_service.dataset.Record;
import com.example.generator_service.dataset.TableSchema;
import com.example.generator_service.dataset.TableSchema.Column;
import com.example.generator_service.export.interfaces.IDataExporter;
import java.io.Writer;
import java.util.Date;
import java.util.StringJoiner;

/**
 * SQL script that loads as is into PostgreSQL: a {@code CREATE TABLE} per entity with its primary key and, for a
 * sub-entity, its foreign key to the parent table, then the rows. Tables come parents first, so every foreign key
 * already has its target when a row is inserted.
 */
public class SQLExporter implements IDataExporter {

    @Override
    public TableWriter open(Writer out) {
        return new StreamingWriter(out) {
            @Override
            protected void startTable(TableSchema schema, TableSchema previous) {
                write((previous == null ? "" : "\n") + createTable(schema));
            }

            @Override
            public void row(Record record) {
                write(insert(current, record));
            }
        };
    }

    static String createTable(TableSchema table) {
        StringJoiner definitions = new StringJoiner(",\n    ", "(\n    ", "\n);\n");
        for (Column column : table.columns()) {
            String definition = identifier(column.name()) + " " + column.type().sql();
            if (column.name().equals(TableSchema.PRIMARY_KEY)) {
                definition += " primary key";
            }
            definitions.add(definition);
        }
        if (table.foreignKey() != null) {
            definitions.add("foreign key (" + identifier(table.foreignKey().column()) + ") references "
                    + identifier(table.foreignKey().referencedTable()) + " (" + identifier(TableSchema.PRIMARY_KEY) + ")");
        }
        return "CREATE TABLE " + identifier(table.name()) + " " + definitions;
    }

    private static String insert(TableSchema table, Record row) {
        StringJoiner columns = new StringJoiner(", ");
        StringJoiner values = new StringJoiner(", ");
        for (Column column : table.columns()) {
            columns.add(identifier(column.name()));
            values.add(literal(row.getValue(column.name())));
        }
        return "INSERT INTO " + identifier(table.name()) + " (" + columns + ") VALUES (" + values + ");\n";
    }

    /** Quoted, so names such as {@code order} or {@code user} are valid table names. */
    private static String identifier(String name) {
        return "\"" + name.replace("\"", "\"\"") + "\"";
    }

    private static String literal(Object value) {
        if (value == null) return "NULL";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        // ISO-8601, which PostgreSQL parses whatever the server's date style.
        if (value instanceof Date date) return "'" + date.toInstant() + "'";
        return "'" + value.toString().replace("'", "''") + "'";
    }
}
