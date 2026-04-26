package com.example.generator_service.generation.strategy;

import net.datafaker.Faker;

/**
 * Stratégie : génération de données d'adresse (adresse, rue, ville, pays, code postal, état).
 */
public class AddressStrategy implements FakerNameStrategy {

    @Override
    public Object generate(Faker faker, String normalizedName) {
        if (matches(normalizedName, "address", "adresse")) {
            return faker.address().fullAddress();
        }
        if (matches(normalizedName, "street", "rue")) {
            return faker.address().streetAddress();
        }
        if (matches(normalizedName, "city", "ville")) {
            return faker.address().city();
        }
        if (matches(normalizedName, "country", "pays")) {
            return faker.address().country();
        }
        if (matches(normalizedName, "zipcode", "codepostal", "postalcode", "cp", "zip")) {
            return faker.address().zipCode();
        }
        if (matches(normalizedName, "state", "region", "état", "etat", "province")) {
            return faker.address().state();
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

