package com.example.microservice.generation.strategy;

import net.datafaker.Faker;

/**
 * Stratégie de génération sémantique par nom d'attribut.
 * Chaque implémentation gère un ensemble de noms reconnus.
 *
 * Respecte le SRP : chaque stratégie a une seule responsabilité (un domaine).
 * Respecte l'OCP : pour ajouter un nouveau domaine, on crée une nouvelle stratégie
 * sans modifier les classes existantes.
 */
public interface FakerNameStrategy {

    /**
     * Tente de générer une valeur pour le nom d'attribut donné.
     *
     * @param faker instance Faker
     * @param normalizedName nom de l'attribut normalisé (lowercase, sans espaces/tirets/underscores)
     * @return la valeur générée, ou null si ce nom n'est pas géré par cette stratégie
     */
    Object generate(Faker faker, String normalizedName);
}

