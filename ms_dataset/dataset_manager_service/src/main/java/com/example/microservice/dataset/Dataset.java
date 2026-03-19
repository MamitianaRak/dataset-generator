package com.example.microservice.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Représente un ensemble de données générées pour un projet.
 * Les enregistrements sont regroupés par nom d'entité.
 */
public class Dataset {

    private final Map<String, List<Record>> records = new ConcurrentHashMap<>();

    /**
     * Ajoute plusieurs enregistrements pour une entité donnée.
     * Si l'entité n'existe pas encore, elle est créée.
     *
     * @param entityName nom de l'entité
     * @param newRecords liste d'enregistrements à ajouter
     */
    public void addRecords(String entityName, List<Record> newRecords) {
        if (entityName == null || newRecords == null) return;
        records.compute(entityName, (k, v) -> {
            if (v == null) v = Collections.synchronizedList(new ArrayList<>());
            v.addAll(newRecords);
            return v;
        });
    }

    /**
     * Ajoute un seul enregistrement pour une entité.
     *
     * @param entityName nom de l'entité
     * @param record enregistrement
     */
    public void addRecord(String entityName, Record record) {
        if (entityName == null || record == null) return;
        records.compute(entityName, (k, v) -> {
            if (v == null) v = Collections.synchronizedList(new ArrayList<>());
            v.add(record);
            return v;
        });
    }

    /**
     * Récupère la liste d'enregistrements pour une entité.
     * Retourne une liste immuable (copie) pour éviter les modifications externes.
     *
     * @param entityName nom de l'entité
     * @return liste d'enregistrements (vide si aucun)
     */
    public List<Record> getRecords(String entityName) {
        List<Record> list = records.get(entityName);
        if (list == null) return Collections.emptyList();
        synchronized (list) {
            return new ArrayList<>(list);
        }
    }

    /**
     * Récupère toutes les entrées (copie superficielle).
     *
     * @return map nomEntité -> liste d'enregistrements
     */
    public Map<String, List<Record>> getAllRecords() {
        return new ConcurrentHashMap<>(records);
    }
}

