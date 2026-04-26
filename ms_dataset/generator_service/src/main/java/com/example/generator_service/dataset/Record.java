package com.example.generator_service.dataset;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Représente une ligne / record d'une entité. Utilise LinkedHashMap
 * pour préserver l'ordre d'insertion des attributs.
 */
public class Record {

    private final Map<String, Object> values = new LinkedHashMap<>();

    public void setValue(String name, Object value) {
        if (name == null) return;
        values.put(name, value);
    }

    public Object getValue(String name) {
        return values.get(name);
    }

    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }
}

