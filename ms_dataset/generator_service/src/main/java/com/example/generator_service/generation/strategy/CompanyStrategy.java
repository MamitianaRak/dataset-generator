package com.example.generator_service.generation.strategy;

import net.datafaker.Faker;

/**
 * Stratégie : génération de données d'entreprise et d'emploi.
 */
public class CompanyStrategy implements FakerNameStrategy {

    @Override
    public Object generate(Faker faker, String normalizedName) {
        if (matches(normalizedName, "company", "entreprise", "societe", "société", "companyname")) {
            return faker.company().name();
        }
        if (matches(normalizedName, "job", "emploi", "métier", "metier", "jobtitle", "poste")) {
            return faker.job().title();
        }
        return null;
    }

    private boolean matches(String name, String... aliases) {
        for (String alias : aliases) {
            if (name.equals(alias) || name.contains(alias)) return true;
        }
        return false;
    }
}

