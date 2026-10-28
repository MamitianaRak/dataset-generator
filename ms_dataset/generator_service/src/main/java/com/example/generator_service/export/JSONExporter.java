package com.example.generator_service.export;

import com.example.generator_service.dataset.Record;
import com.example.generator_service.dataset.TableSchema;
import com.example.generator_service.export.interfaces.IDataExporter;
import java.io.Writer;
import java.util.Date;
import java.util.Map;

/** {@code {"table": [{...}, ...], ...}}, written row by row. */
public class JSONExporter implements IDataExporter {

    @Override
    public TableWriter open(Writer out) {
        return new StreamingWriter(out) {
            private boolean firstRow;

            @Override
            protected void startTable(TableSchema schema, TableSchema previous) {
                write(previous == null ? "{" : "],");
                write(string(schema.name()) + ":[");
                firstRow = true;
            }

            @Override
            public void row(Record record) {
                StringBuilder json = new StringBuilder(firstRow ? "{" : ",{");
                boolean firstField = true;
                for (Map.Entry<String, Object> field : record.getValues().entrySet()) {
                    json.append(firstField ? "" : ",").append(string(field.getKey())).append(':').append(value(field.getValue()));
                    firstField = false;
                }
                write(json.append('}').toString());
                firstRow = false;
            }

            @Override
            public void finish() {
                write(current == null ? "{}" : "]}");
                super.finish();
            }
        };
    }

    private static String value(Object value) {
        if (value == null) return "null";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Date date) return string(date.toInstant().toString());
        return string(value.toString());
    }

    private static String string(String s) {
        StringBuilder json = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (c < 0x20) {
                        json.append(String.format("\\u%04x", (int) c));
                    } else {
                        json.append(c);
                    }
                }
            }
        }
        return json.append('"').toString();
    }
}
