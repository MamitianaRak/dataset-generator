package com.example.microservice.generation.strategy;

import net.datafaker.Faker;

/**
 * Stratégie : génération de données de contact (email, téléphone).
 */
public class ContactStrategy implements FakerNameStrategy {

    @Override
    public Object generate(Faker faker, String normalizedName) {
        if (matches(normalizedName, "email", "mail", "courriel")) {
            return faker.internet().emailAddress();
        }
        if (matches(normalizedName, "phone", "telephone", "téléphone", "tel", "phonenumber", "mobile")) {
            return faker.phoneNumber().cellPhone();
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

