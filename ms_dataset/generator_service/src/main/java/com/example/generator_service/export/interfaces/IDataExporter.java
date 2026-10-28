package com.example.generator_service.export.interfaces;

import com.example.generator_service.dataset.Dataset;
import com.example.generator_service.dataset.RowSink;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 * Writes a dataset in one format, row by row as it is generated: no temporary file, nothing kept in memory.
 */
public interface IDataExporter {

    /** A writer for this format on {@code out}; {@link TableWriter#finish} closes the document. */
    TableWriter open(Writer out);

    /** Writes a dataset already in memory (tests, small datasets). */
    default void export(Dataset dataset, Writer out) throws IOException {
        TableWriter writer = open(out);
        try {
            dataset.replay(writer);
            writer.finish();
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    /** Receives the tables and rows to write; IO errors surface as {@link UncheckedIOException}. */
    interface TableWriter extends RowSink {

        /** Writes what closes the document (brackets, end tags) and flushes. */
        void finish();
    }
}
