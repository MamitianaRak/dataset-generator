package com.example.microservice.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.example.microservice.dataset.Dataset;
import com.example.microservice.dataset.Record;

/**
 * Export CSV basique. Utilise Dataset et Record.
 */
public class CSVExporter implements com.example.microservice.export.interfaces.IDataExporter {

    @Override
    public void export(Dataset dataset, String outputPath) throws IOException {
        if (dataset == null) throw new IllegalArgumentException("Dataset ne peut pas être null");

        try (FileWriter fw = new FileWriter(outputPath)) {
            boolean firstEntity = true;
            for (Map.Entry<String, List<Record>> entry : dataset.getAllRecords().entrySet()) {
                if (!firstEntity) {
                    fw.write("\n");
                }
                firstEntity = false;
                String entityName = entry.getKey();
                List<Record> records = entry.getValue();

                fw.write("# Entity: " + entityName + "\n");
                if (records.isEmpty()) continue;

                Map<String, Object> first = records.get(0).getValues();
                boolean firstCol = true;
                for (String col : first.keySet()) {
                    if (!firstCol) fw.write(",");
                    firstCol = false;
                    fw.write(escape(col));
                }
                fw.write("\n");

                for (Record r : records) {
                    firstCol = true;
                    for (Object v : r.getValues().values()) {
                        if (!firstCol) fw.write(",");
                        firstCol = false;
                        fw.write(escape(String.valueOf(v)));
                    }
                    fw.write("\n");
                }
            }
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\n") || s.contains("\"") ) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }
}
