package com.example.generator_service.dataset;

/**
 * Receives generated rows as they are produced, table by table: {@link #table} announces a table, the
 * {@link #row} calls that follow belong to it. An exporter writes each row out and forgets it, so an export uses
 * the same memory for a thousand rows or a million.
 */
public interface RowSink {

    void table(TableSchema schema);

    void row(Record record);
}
