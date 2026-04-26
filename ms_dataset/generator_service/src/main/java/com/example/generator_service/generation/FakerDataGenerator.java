package com.example.generator_service.generation;

import com.example.generator_service.dto.entity.AttributeDTO;
import com.example.generator_service.dto.model.constraints.ConstraintDTO;
import com.example.generator_service.dto.model.constraints.MinMaxConstraintDTO;
import com.example.generator_service.generation.interfaces.IDataGenerator;
import com.example.generator_service.generation.strategy.*;
import lombok.*;
import net.datafaker.Faker;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Générateur de données réalistes utilisant la bibliothèque Datafaker.
 * Service stateless qui génère des données à partir de DTOs uniquement.
 * <p>
 * Délègue la résolution sémantique par nom d'attribut à une liste de {@link FakerNameStrategy}.
 * Pour ajouter un nouveau domaine, il suffit de créer une nouvelle stratégie
 * et de l'ajouter à la liste (respect du principe OCP).
 * </p>
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FakerDataGenerator implements IDataGenerator {

    /** Locale utilisée par Faker (ex: "fr", "en", "en-US") */
    private String fakerLocale = "fr";

    private transient Faker faker;

    /**
     * Liste des stratégies de génération par nom.
     * OCP : pour étendre, ajouter une stratégie ici sans modifier le code existant.
     */
    private transient List<FakerNameStrategy> nameStrategies;

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

    // ──────────────────────────────────────────────────────────────
    //  Utilitaires
    // ──────────────────────────────────────────────────────────────

    private String normalize(String name) {
        if (name == null) return "";
        return name.replaceAll("[\\s_-]", "").toLowerCase();
    }

    // ──────────────────────────────────────────────────────────────
    //  Génération à partir de DTOs (seule méthode publique)
    // ──────────────────────────────────────────────────────────────

    @Override
    public Object generateFromDTO(AttributeDTO attribute) {
        if (attribute == null) throw new IllegalArgumentException("attribute == null");

        Faker f = getFaker();
        String name = normalize(attribute.getName());

        // ── 1. Délégation aux stratégies par nom ──
        for (FakerNameStrategy strategy : getStrategies()) {
            Object value = strategy.generate(f, name);
            if (value != null) return value;
        }

        // ── 2. Fallback : génération par type + contraintes ──
        return generateByTypeFromDTO(f, attribute);
    }

    private Object generateByTypeFromDTO(Faker f, AttributeDTO attribute) {
        var typeDTO = attribute.getType();
        if (typeDTO == null) return f.lorem().word();

        // Extraction des contraintes min/max depuis le DTO
        Double min = null;
        Double max = null;
        if (attribute.getConstraints() != null) {
            for (ConstraintDTO constraint : attribute.getConstraints()) {
                if (constraint instanceof MinMaxConstraintDTO minMaxConstraint) {
                    min = minMaxConstraint.getMin();
                    max = minMaxConstraint.getMax();
                    break;
                }
            }
        }

        // Déterminer le type en utilisant le nom de la classe
        String typeClass = typeDTO.getClass().getSimpleName().replace("DTO", "");

        switch (typeClass) {
            case "IntegerType":
                int minInt = (min != null) ? min.intValue() : 0;
                int maxInt = (max != null) ? max.intValue() : 10000;
                return f.number().numberBetween(minInt, maxInt);

            case "FloatType":
                double minDouble = (min != null) ? min : 0.0;
                double maxDouble = (max != null) ? max : 10000.0;
                return f.number().randomDouble(2, (long) minDouble, (long) maxDouble);

            case "BooleanType":
                return f.bool().bool();

            case "DateType":
                return f.date().past(3650, TimeUnit.DAYS);

            case "EnumType":
                // Cast vers EnumTypeDTO pour accéder aux valeurs
                if (typeDTO instanceof com.example.generator_service.dto.model.types.EnumTypeDTO enumType) {
                    var values = enumType.getValues();
                    if (values != null && !values.isEmpty()) {
                        return values.get(f.random().nextInt(values.size()));
                    }
                }
                return null;

            case "StringType":
                int maxLen = (max != null) ? max.intValue() : 20;
                return f.lorem().characters(1, Math.max(2, maxLen));

            default:
                return f.lorem().word();
        }
    }
}
