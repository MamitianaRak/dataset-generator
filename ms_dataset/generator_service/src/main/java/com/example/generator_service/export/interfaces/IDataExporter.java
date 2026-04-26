package com.example.generator_service.export.interfaces;

import java.io.IOException;
import com.example.generator_service.dataset.Dataset;

/**
 * Interface pour exporter un dataset. L'implémentation concrète doit
 * gérer la sérialisation et l'écriture sur disque.
 */
public interface IDataExporter {
    /**
     * Exporte le dataset donné vers le chemin de sortie spécifié.
     *
     * @param dataset objet représentant le dataset
     * @param outputPath chemin du fichier de sortie
     * @throws IOException en cas d'erreur d'écriture
     */
    void export(Dataset dataset, String outputPath) throws IOException;
}
