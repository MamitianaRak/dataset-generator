package com.example.generator_service.dataset;

import java.util.List;

/**
 * Shape of a generated table: what the SQL export needs to create it before inserting rows. Every table has an
 * {@code id} key; a sub-entity's table also has a foreign key to its parent's table.
 */
public record TableSchema(String name, List<Column> columns, ForeignKey foreignKey) {

    public static final String PRIMARY_KEY = "id";

    public TableSchema {
        columns = List.copyOf(columns);
    }

    public record Column(String name, SqlType type) {
    }

    /** {@code column} holds the {@code id} of a row of {@code referencedTable}. */
    public record ForeignKey(String column, String referencedTable) {
    }

    /** Column types, chosen from the values actually generated (a name-based generator may return text). */
    public enum SqlType {
        BIGINT("bigint"),
        DOUBLE("double precision"),
        BOOLEAN("boolean"),
        TIMESTAMP("timestamp"),
        TEXT("text");

        private final String sql;

        SqlType(String sql) {
            this.sql = sql;
        }

        public String sql() {
            return sql;
        }
    }
}
