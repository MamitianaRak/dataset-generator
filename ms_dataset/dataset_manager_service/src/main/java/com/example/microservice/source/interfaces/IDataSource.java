package com.example.microservice.source.interfaces;

import java.util.List;

/**
 * Interface pour récupérer des données externes (listes de valeurs pour un attribut, par ex.).
 */
public interface IDataSource {
    /**
     * Récupère les données disponibles dans cette source.
     * @return liste de chaînes (peut être vide mais jamais null)
     */
    List<String> fetchData();
}
