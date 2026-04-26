package com.example.generator_service.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.example.generator_service.dataset.Dataset;
import com.example.generator_service.dataset.Record;

/**
 * Export SQL simple : génère des INSERT INTO pour chaque enregistrement.
 */
public class SQLExporter implements com.example.generator_service.export.interfaces.IDataExporter {

    @Override
    public void export(Dataset dataset, String outputPath) throws IOException {
        if (dataset == null) throw new IllegalArgumentException("Dataset ne peut pas être null");

        try (FileWriter fw = new FileWriter(outputPath)) {
            for (Map.Entry<String, List<Record>> entry : dataset.getAllRecords().entrySet()) {
                String table = entry.getKey();
                List<Record> records = entry.getValue();
                for (Record r : records) {
                    StringJoiner cols = new StringJoiner(", ");
                    StringJoiner vals = new StringJoiner(", ");
                    for (Map.Entry<String, Object> c : r.getValues().entrySet()) {
                        cols.add(escapeIdentifier(c.getKey()));
                        vals.add(formatValue(c.getValue()));
                    }
                    fw.write(String.format("INSERT INTO %s (%s) VALUES (%s);\n", escapeIdentifier(table), cols.toString(), vals.toString()));
                }
            }
        }
    }

    private String escapeIdentifier(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private String formatValue(Object v) {
        if (v == null) return "NULL";
        if (v instanceof Number || v instanceof Boolean) return v.toString();
        return "'" + v.toString().replace("'", "''") + "'";
    }
}
