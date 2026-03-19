package com.example.microservice.export;

import com.example.microservice.export.interfaces.IDataExporter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory pour créer des exporters selon le format demandé.
 * Respecte OCP : ajouter un format = ajouter une entrée dans les maps.
 */
@Component
public class ExporterFactory {

    private final Map<String, IDataExporter> exporters = Map.of(
            "json", new JSONExporter(),
            "xml", new XMLExporter(),
            "sql", new SQLExporter(),
            "csv", new CSVExporter()
    );

    private final Map<String, MediaType> mediaTypes = Map.of(
            "json", MediaType.APPLICATION_JSON,
            "xml", MediaType.APPLICATION_XML,
            "sql", MediaType.parseMediaType("application/sql"),
            "csv", MediaType.parseMediaType("text/csv")
    );

    private final Map<String, String> extensions = Map.of(
            "json", ".json",
            "xml", ".xml",
            "sql", ".sql",
            "csv", ".csv"
    );

    public IDataExporter getExporter(String format) {
        IDataExporter exporter = exporters.get(format.toLowerCase());
        if (exporter == null) {
            throw new IllegalArgumentException("Unsupported export format: " + format);
        }
        return exporter;
    }

    public MediaType getMediaType(String format) {
        MediaType type = mediaTypes.get(format.toLowerCase());
        return type != null ? type : MediaType.APPLICATION_OCTET_STREAM;
    }

    public String getExtension(String format) {
        String ext = extensions.get(format.toLowerCase());
        return ext != null ? ext : "." + format.toLowerCase();
    }
}
