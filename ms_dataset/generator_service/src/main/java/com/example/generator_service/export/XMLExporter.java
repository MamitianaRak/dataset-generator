package com.example.generator_service.export;

import com.example.generator_service.dataset.Record;
import com.example.generator_service.dataset.TableSchema;
import com.example.generator_service.export.interfaces.IDataExporter;
import java.io.Writer;
import java.util.Map;

/** {@code <dataset><entity name="..."><record>...</record></entity></dataset>}, written row by row. */
public class XMLExporter implements IDataExporter {

    @Override
    public TableWriter open(Writer out) {
        return new StreamingWriter(out) {
            {
                write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<dataset>\n");
            }

            @Override
            protected void startTable(TableSchema schema, TableSchema previous) {
                write((previous == null ? "" : "  </entity>\n") + "  <entity name=\"" + escape(schema.name()) + "\">\n");
            }

            @Override
            public void row(Record record) {
                StringBuilder xml = new StringBuilder("    <record>\n");
                for (Map.Entry<String, Object> field : record.getValues().entrySet()) {
                    String tag = escape(field.getKey());
                    xml.append("      <").append(tag).append('>').append(escape(String.valueOf(field.getValue())))
                            .append("</").append(tag).append(">\n");
                }
                write(xml.append("    </record>\n").toString());
            }

            @Override
            public void finish() {
                write((current == null ? "" : "  </entity>\n") + "</dataset>\n");
                super.finish();
            }
        };
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}
