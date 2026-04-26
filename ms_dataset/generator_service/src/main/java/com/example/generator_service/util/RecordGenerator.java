package com.example.generator_service.util;

import com.example.generator_service.dto.entity.AttributeDTO;
import com.example.generator_service.dto.entity.DataEntityResponseDTO;
import com.example.generator_service.generation.interfaces.IDataGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe utilitaire stateless qui génère des enregistrements à partir de DTOs.
 * Utilisé par le generator-service pour créer des données aléatoires.
 */
public class RecordGenerator {
    private final IDataGenerator generator;

    public RecordGenerator(IDataGenerator generator) {
        this.generator = generator;
    }

    /**
     * Génère un enregistrement (map attribut->valeur) pour l'entité donnée (DTO).
     * Utilise le générateur global du service (stateless).
     */
    public Map<String, Object> generateRecordFromDTO(DataEntityResponseDTO entity) {
        Map<String, Object> record = new HashMap<>();
        if (entity == null || entity.getAttributes() == null) return record;

        for (AttributeDTO a : entity.getAttributes()) {
            // Utilise toujours le générateur global (service stateless)
            Object value = (generator != null) ? generator.generateFromDTO(a) : null;
            record.put(a.getName(), value);
        }
        return record;
    }

    /**
     * Génère plusieurs enregistrements pour l'entité (count lignes) - version DTO.
     */
    public List<Map<String, Object>> generateFromDTO(DataEntityResponseDTO entity, int count) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rows.add(generateRecordFromDTO(entity));
        }
        return rows;
    }
}
