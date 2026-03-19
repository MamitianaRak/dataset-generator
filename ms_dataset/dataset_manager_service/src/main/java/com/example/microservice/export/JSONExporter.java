package com.example.microservice.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.example.microservice.dataset.Dataset;
import com.example.microservice.dataset.Record;

/**
 * Export JSON minimal. Utilise Dataset.
 */
public class JSONExporter implements com.example.microservice.export.interfaces.IDataExporter {

    @Override
    public void export(Dataset dataset, String outputPath) throws IOException {
        if (dataset == null) throw new IllegalArgumentException("Dataset ne peut pas être null");

        try (FileWriter fw = new FileWriter(outputPath)) {
            fw.write(toJson(dataset.getAllRecords()));
        }
    }

    private String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return '"' + escape((String) obj) + '"';
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Record) {
            Record record = (Record) obj;
            return toJson(record.getValues());
        }
        if (obj instanceof Map) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            boolean first = true;
            @SuppressWarnings("unchecked")
            Map<Object, Object> m = (Map<Object, Object>) obj;
            for (Map.Entry<Object, Object> e : m.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(String.valueOf(e.getKey()))).append(":").append(toJson(e.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof Iterable) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            for (Object o : (Iterable<?>) obj) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(o));
            }
            sb.append("]");
            return sb.toString();
        }
        return '"' + escape(obj.toString()) + '"';
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
