package com.example.microservice.generation.strategy;

import net.datafaker.Faker;

import java.util.concurrent.TimeUnit;

/**
 * Stratégie : génération de données commerciales (prix, produit),
 * de dates, d'âge, de genre, de couleur, de codes (ISBN, EAN), d'avatar.
 */
public class CommerceAndMiscStrategy implements FakerNameStrategy {

    @Override
    public Object generate(Faker faker, String normalizedName) {
        // Commerce
        if (matches(normalizedName, "price", "prix", "amount", "montant", "cost", "cout", "coût")) {
            String priceStr = faker.commerce().price(1.0, 999.99).replace(",", ".");
            return Double.parseDouble(priceStr);
        }
        if (matches(normalizedName, "product", "produit", "productname")) {
            return faker.commerce().productName();
        }

        // Dates
        if (matches(normalizedName, "birthdate", "datenaissance", "birthday", "dateofbirth", "dob")) {
            return faker.date().birthday();
        }
        if (matches(normalizedName, "date", "createdat", "updatedat", "created", "updated")) {
            return faker.date().past(365, TimeUnit.DAYS);
        }

        // Age
        if (matches(normalizedName, "age", "âge")) {
            return faker.number().numberBetween(1, 100);
        }

        // Genre
        if (matches(normalizedName, "gender", "genre", "sexe")) {
            return faker.gender().shortBinaryTypes();
        }

        // Divers
        if (matches(normalizedName, "color", "couleur")) {
            return faker.color().name();
        }
        if (matches(normalizedName, "isbn")) {
            return faker.code().isbn13();
        }
        if (matches(normalizedName, "ean", "barcode")) {
            return faker.code().ean13();
        }
        if (matches(normalizedName, "avatar", "image", "photo")) {
            return faker.avatar().image();
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

