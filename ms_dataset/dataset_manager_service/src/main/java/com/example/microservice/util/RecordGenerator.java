package com.example.microservice.util;

import com.example.microservice.generation.interfaces.IDataGenerator;
import com.example.microservice.model.entities.Attribute;
import com.example.microservice.model.entities.DataEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe utilitaire qui génère des enregistrements pour une DataEntity à l'aide d'un IDataGenerator.
 * Cette classe n'est pas un Spring Service, c'est un helper pour la génération de données.
 */
public class RecordGenerator {
    private final IDataGenerator generator;

    public RecordGenerator(IDataGenerator generator) {
        this.generator = generator;
    }

    /**
     * Génère un enregistrement (map attribut->valeur) pour l'entité donnée.
     * Priorité : générateur de l'attribut > générateur global du service
     */
    public Map<String, Object> generateRecord(DataEntity entity) {
        Map<String, Object> record = new HashMap<>();
        if (entity == null) return record;
        for (Attribute a : entity.getAttributes()) {
            // Priorité au générateur spécifique de l'attribut
            IDataGenerator genToUse = (a.getGenerator() != null) ? a.getGenerator() : generator;
            Object value = (genToUse != null) ? genToUse.generate(a) : null;
            record.put(a.getName(), value);
        }
        return record;
    }

    /**
     * Génère plusieurs enregistrements pour l'entité (count lignes).
     */
    public List<Map<String, Object>> generate(DataEntity entity, int count) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rows.add(generateRecord(entity));
        }
        return rows;
    }
}
