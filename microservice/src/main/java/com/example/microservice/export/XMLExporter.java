package com.example.microservice.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.example.microservice.dataset.Dataset;
import com.example.microservice.dataset.Record;

/**
 * Export XML basique. Utilise Dataset et Record.
 */
public class XMLExporter implements com.example.microservice.export.interfaces.IDataExporter {

    @Override
    public void export(Dataset dataset, String outputPath) throws IOException {
        if (dataset == null) throw new IllegalArgumentException("Dataset ne peut pas être null");

        try (FileWriter fw = new FileWriter(outputPath)) {
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            fw.write("<dataset>\n");
            for (Map.Entry<String, List<Record>> entry : dataset.getAllRecords().entrySet()) {
                String entityName = entry.getKey();
                List<Record> records = entry.getValue();
                fw.write("  <entity name=\"" + escape(entityName) + "\">\n");
                for (Record r : records) {
                    fw.write("    <record>\n");
                    for (Map.Entry<String, Object> col : r.getValues().entrySet()) {
                        fw.write("      <" + escape(col.getKey()) + ">" + escape(String.valueOf(col.getValue())) + "</" + escape(col.getKey()) + ">\n");
                    }
                    fw.write("    </record>\n");
                }
                fw.write("  </entity>\n");
            }
            fw.write("</dataset>\n");
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}
