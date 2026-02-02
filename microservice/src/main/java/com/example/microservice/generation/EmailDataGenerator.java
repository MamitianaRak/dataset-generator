package com.example.microservice.generation;

import com.example.microservice.model.entities.Attribute;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.util.Random;

/**
 * Générateur d'emails aléatoires.
 * Génère des emails avec des prénoms, noms et domaines aléatoires.
 */
@Entity
@DiscriminatorValue("EmailDataGenerator")
public class EmailDataGenerator extends BaseDataGenerator {

    private static final String[] FIRST_NAMES = {
        "john", "jane", "alice", "bob", "charlie", "david", "emma", "frank",
        "grace", "henry", "isabelle", "jack", "kate", "leo", "maria", "nathan",
        "olivia", "peter", "quinn", "rachel", "sam", "tara", "uma", "victor"
    };

    private static final String[] LAST_NAMES = {
        "smith", "johnson", "williams", "brown", "jones", "garcia", "miller",
        "davis", "rodriguez", "martinez", "hernandez", "lopez", "gonzalez",
        "wilson", "anderson", "thomas", "taylor", "moore", "jackson", "martin"
    };

    private static final String[] DOMAINS = {
        "gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "icloud.com",
        "example.com", "test.com", "mail.com", "email.com", "demo.com"
    };

    @Transient
    private final Random random = new Random();

    @Override
    public Object generate(Attribute attribute) {
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String domain = DOMAINS[random.nextInt(DOMAINS.length)];

        // Format: firstname.lastname@domain.com
        return firstName + "." + lastName + "@" + domain;
    }
}
