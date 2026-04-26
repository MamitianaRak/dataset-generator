package com.example.generator_service.generation.strategy;

import net.datafaker.Faker;

/**
 * Stratégie : génération de données d'identité (prénom, nom, nom complet, username).
 */
public class IdentityStrategy implements FakerNameStrategy {

    @Override
    public Object generate(Faker faker, String normalizedName) {
        if (matches(normalizedName, "firstname", "prenom", "prénom")) {
            return faker.name().firstName();
        }
        if (matches(normalizedName, "lastname", "nomfamille", "surname")) {
            return faker.name().lastName();
        }
        if (matches(normalizedName, "fullname", "nomcomplet", "name")) {
            return faker.name().fullName();
        }
        if (matches(normalizedName, "username", "login", "pseudo")) {
            return faker.internet().username();
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

