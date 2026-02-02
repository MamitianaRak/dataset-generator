package com.example.microservice.distribution.interfaces;

import com.example.microservice.model.interfaces.IConstraint;
import com.example.microservice.model.interfaces.IDataType;

/**
 * Interface représentant une distribution capable de produire une valeur pour un type et
 * éventuellement en tenant compte d'une contrainte.
 */
public interface IDistribution {
    /**
     * Génère une valeur suivant la distribution pour le type et la contrainte fournis.
     * @param dataType type cible (peut être utilisé pour conversion ou génération)
     * @param constraint contrainte optionnelle
     * @return valeur générée (peut être null)
     */
    Object nextValue(IDataType dataType, IConstraint constraint);
}
