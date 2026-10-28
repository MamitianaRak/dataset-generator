package com.example.generator_service.export;

import com.example.generator_service.dataset.TableSchema;
import com.example.generator_service.export.interfaces.IDataExporter.TableWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

/** Base of the format writers: keeps the current table and turns IO errors into unchecked ones. */
abstract class StreamingWriter implements TableWriter {

    private final Writer out;
    protected TableSchema current;

    StreamingWriter(Writer out) {
        this.out = out;
    }

    @Override
    public final void table(TableSchema schema) {
        TableSchema previous = current;
        current = schema;
        startTable(schema, previous);
    }

    /** {@code previous} is null for the first table. */
    protected abstract void startTable(TableSchema schema, TableSchema previous);

    @Override
    public void finish() {
        try {
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void write(String text) {
        try {
            out.write(text);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
