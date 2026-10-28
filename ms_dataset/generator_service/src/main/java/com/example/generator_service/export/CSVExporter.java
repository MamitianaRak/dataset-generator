package com.example.generator_service.export;

import com.example.generator_service.dataset.Record;
import com.example.generator_service.dataset.TableSchema;
import com.example.generator_service.dataset.TableSchema.Column;
import com.example.generator_service.export.interfaces.IDataExporter;
import java.io.Writer;
import java.util.StringJoiner;

/** One block per table: a {@code # Entity:} line, the header, then the rows. */
public class CSVExporter implements IDataExporter {

    @Override
    public TableWriter open(Writer out) {
        return new StreamingWriter(out) {
            @Override
            protected void startTable(TableSchema schema, TableSchema previous) {
                StringJoiner header = new StringJoiner(",", (previous == null ? "" : "\n") + "# Entity: " + schema.name() + "\n", "\n");
                schema.columns().forEach(column -> header.add(escape(column.name())));
                write(header.toString());
            }

            @Override
            public void row(Record record) {
                StringJoiner line = new StringJoiner(",", "", "\n");
                for (Column column : current.columns()) {
                    Object value = record.getValue(column.name());
                    line.add(value == null ? "" : escape(value.toString()));
                }
                write(line.toString());
            }
        };
    }

    private static String escape(String s) {
        if (s.contains(",") || s.contains("\n") || s.contains("\"")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }
}
