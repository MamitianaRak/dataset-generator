package com.example.microservice.generation.strategy;

import net.datafaker.Faker;

/**
 * Stratégie : génération de données internet (URL, IP, mot de passe, UUID)
 * et de texte (description, titre, contenu).
 */
public class InternetAndTextStrategy implements FakerNameStrategy {

    @Override
    public Object generate(Faker faker, String normalizedName) {
        // Internet
        if (matches(normalizedName, "url", "website", "siteweb", "site", "web")) {
            return faker.internet().url();
        }
        if (matches(normalizedName, "ip", "ipaddress")) {
            return faker.internet().ipV4Address();
        }
        if (matches(normalizedName, "password", "motdepasse", "mdp")) {
            return faker.internet().password(8, 20);
        }
        if (matches(normalizedName, "uuid")) {
            return faker.internet().uuid();
        }

        // Texte
        if (matches(normalizedName, "description", "bio", "about", "summary", "résumé", "resume")) {
            return faker.lorem().paragraph();
        }
        if (matches(normalizedName, "title", "titre")) {
            return faker.lorem().sentence(3);
        }
        if (matches(normalizedName, "text", "texte", "content", "contenu", "body", "corps")) {
            return faker.lorem().paragraph(2);
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

