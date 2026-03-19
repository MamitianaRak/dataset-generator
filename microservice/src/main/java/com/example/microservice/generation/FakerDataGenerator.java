package com.example.microservice.generation;

import com.example.microservice.generation.interfaces.IDataGenerator;
import com.example.microservice.generation.strategy.*;
import com.example.microservice.model.constraints.MinMaxConstraint;
import com.example.microservice.model.entities.Attribute;
import com.example.microservice.model.interfaces.IConstraint;
import com.example.microservice.model.types.*;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.*;
import net.datafaker.Faker;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Générateur de données réalistes utilisant la bibliothèque Datafaker.
 * <p>
 * Délègue la résolution sémantique par nom d'attribut à une liste de {@link FakerNameStrategy}.
 * Pour ajouter un nouveau domaine, il suffit de créer une nouvelle stratégie
 * et de l'ajouter à la liste (respect du principe OCP).
 * </p>
 * <p>
 * SRP : cette classe orchestre les stratégies et gère le fallback par type.
 * Chaque stratégie a sa propre responsabilité (un domaine de données).
 * </p>
 */
@Entity
@DiscriminatorValue("FAKER")
@NoArgsConstructor
@Getter
@Setter
public class FakerDataGenerator extends BaseDataGenerator implements IDataGenerator {

    /** Locale utilisée par Faker (ex: "fr", "en", "en-US") */
    private String fakerLocale = "fr";

    @Transient
    private Faker faker;

    /**
     * Liste des stratégies de génération par nom.
     * OCP : pour étendre, ajouter une stratégie ici sans modifier le code existant.
     */
    @Transient
    private List<FakerNameStrategy> nameStrategies;

    public FakerDataGenerator(String locale) {
        this.fakerLocale = (locale != null) ? locale : "fr";
    }

    private Faker getFaker() {
        if (faker == null) {
            faker = new Faker(Locale.of(fakerLocale != null ? fakerLocale : "fr"));
        }
        return faker;
    }

    private List<FakerNameStrategy> getStrategies() {
        if (nameStrategies == null) {
            nameStrategies = List.of(
                new IdentityStrategy(),
                new ContactStrategy(),
                new AddressStrategy(),
                new CompanyStrategy(),
                new InternetAndTextStrategy(),
                new CommerceAndMiscStrategy()
            );
        }
        return nameStrategies;
    }

    @Override
    public Object generate(Attribute attribute) {
        if (attribute == null) throw new IllegalArgumentException("attribute == null");

        Faker f = getFaker();
        String name = normalize(attribute.getName());

        // ── 1. Délégation aux stratégies par nom ──
        for (FakerNameStrategy strategy : getStrategies()) {
            Object value = strategy.generate(f, name);
            if (value != null) return value;
        }

        // ── 2. Fallback : génération par type + contraintes ──
        return generateByType(f, attribute);
    }

    // ──────────────────────────────────────────────────────────────
    //  Génération basée sur le type de l'attribut (fallback)
    // ──────────────────────────────────────────────────────────────

    private Object generateByType(Faker f, Attribute attribute) {
        var type = attribute.getType();
        if (type == null) return f.lorem().word();

        MinMaxConstraint minMax = findMinMaxConstraint(attribute);

        if (type instanceof IntegerType) {
            int min = (minMax != null) ? (int) minMax.getMin() : 0;
            int max = (minMax != null) ? (int) minMax.getMax() : 10000;
            return f.number().numberBetween(min, max);
        }

        if (type instanceof FloatType) {
            double min = (minMax != null) ? minMax.getMin() : 0.0;
            double max = (minMax != null) ? minMax.getMax() : 10000.0;
            return f.number().randomDouble(2, (long) min, (long) max);
        }

        if (type instanceof BooleanType) {
            return f.bool().bool();
        }

        if (type instanceof DateType) {
            return f.date().past(3650, TimeUnit.DAYS);
        }

        if (type instanceof com.example.microservice.model.types.EnumType enumType) {
            var values = enumType.getValues();
            if (values != null && !values.isEmpty()) {
                return values.get(f.random().nextInt(values.size()));
            }
            return null;
        }

        if (type instanceof StringType) {
            int maxLen = (minMax != null) ? (int) minMax.getMax() : 20;
            return f.lorem().characters(1, Math.max(2, maxLen));
        }

        return f.lorem().word();
    }

    // ──────────────────────────────────────────────────────────────
    //  Utilitaires
    // ──────────────────────────────────────────────────────────────

    private String normalize(String name) {
        if (name == null) return "";
        return name.replaceAll("[\\s_-]", "").toLowerCase();
    }

    private MinMaxConstraint findMinMaxConstraint(Attribute attribute) {
        if (attribute.getConstraints() == null) return null;
        for (IConstraint constraint : attribute.getConstraints()) {
            if (constraint instanceof MinMaxConstraint) {
                return (MinMaxConstraint) constraint;
            }
        }
        return null;
    }
}
