# Bruno Collection - Spring REST Microservice API

Collection de tests pour l'API REST du microservice de génération de données.

## Configuration

### Variables d'environnement (MicroService)
- `baseUrl`: http://localhost:8081/api
- `projectId`: 1 (pour tests unitaires manuels)
- `entityId`: 1 (pour tests unitaires manuels)
- `current_project_id`: (géré automatiquement par le scénario d'intégration)
- `current_entity_id`: (géré automatiquement par le scénario d'intégration)

## Structure de la Collection

### 1. IntegrationScenario (Dossier C - Scénario Nominal)
**Test d'intégration complet rejouable à l'infini**

Ce scénario s'exécute de bout en bout automatiquement sans intervention manuelle :

1. **01 - Create Project** - Crée un projet avec timestamp unique
2. **02 - Add User Entity** - Ajoute une entité User avec 4 attributs
3. **03 - Update Entity** - Modifie l'entité User (ajout attribut isActive)
4. **04 - Add Product Entity** - Ajoute une entité Product avec 6 attributs
5. **05 - Preview Data** - Prévisualise les données générées (5 items)
6. **06 - Export JSON** - Exporte les données en JSON (10 items)
7. **07 - Export XML** - Exporte les données en XML (10 items)
8. **08 - List Project Entities** - Vérifie que les 2 entités existent
9. **09 - Cleanup - Delete Project** - Supprime le projet (nettoyage)

**Caractéristiques :**
- ✅ Utilise des variables d'environnement pour chaîner les requêtes
- ✅ Rejouable à l'infini (timestamp unique + nettoyage)
- ✅ 100% automatisé (pas d'intervention manuelle)
- ✅ Couvre tous les endpoints principaux

### 2. ErrorHandling (Dossier B - Gestion des Erreurs)
Tests des cas d'erreur et validation des messages d'erreur :

- **404 - Get Project Not Found** - Projet inexistant (ID 999999)
- **404 - Get Entity Not Found** - Entité inexistante (ID 999999)
- **404 - Delete Project Not Found** - Suppression projet inexistant
- **400 - Create Project Without Name** - Validation : nom requis
- **400 - Create Entity Invalid Constraint** - Contrainte invalide (min > max)
- **400 - Create Entity Unknown Type** - Type de champ inconnu

### 3. Projects (Dossier A - Tests Unitaires)
Tests isolés des endpoints Projects :

1. **Create Project** - Crée un nouveau projet
2. **List Projects** - Liste tous les projets
3. **Get Project** - Récupère les détails d'un projet
4. **Update Project** - Modifie le nom d'un projet
5. **Delete Project** - Supprime un projet

### 4. Entities (Dossier A - Tests Unitaires)
Tests isolés des endpoints Entities :

1. **Create Entity - User** - Crée une entité User avec attributs polymorphiques
2. **Create Entity - Product** - Crée une entité Product avec tous les types
3. **List Entities in Project** - Liste toutes les entités d'un projet
4. **Get Entity** - Récupère les détails d'une entité
5. **Update Entity** - Modifie une entité
6. **Delete Entity** - Supprime une entité

### 5. Generation (Export)
Tests d'export et génération :

1. **Preview Data** - Génère un aperçu JSON des données
2. **Export JSON** - Télécharge les données en format JSON
3. **Export XML** - Télécharge les données en format XML

## Installation et Exécution

### Prérequis
- Node.js (v14 ou supérieur)
- Application Spring Boot démarrée sur http://localhost:8081

### Installation Bruno CLI

```bash
npm install -g @usebruno/cli
```

### Exécution des Tests

#### Lancer tous les tests
```bash
cd microservice/tests/bruno-collection
bru run --env MicroService
```

#### Lancer uniquement le scénario d'intégration
```bash
cd microservice/tests/bruno-collection
bru run --folder IntegrationScenario --env MicroService
```

#### Lancer uniquement les tests d'erreur
```bash
cd microservice/tests/bruno-collection
bru run --folder ErrorHandling --env MicroService
```

#### Générer un rapport HTML
```bash
cd microservice/tests/bruno-collection
bru run --env MicroService --output results.json
# Note: Bruno CLI génère un rapport texte par défaut. Pour HTML, utilisez un outil tiers ou l'interface Bruno
```

### Résultats Attendus

✅ **Scénario d'intégration** : 9 requêtes, tous les tests passent
✅ **Gestion des erreurs** : 6 requêtes, tous les tests passent
✅ **Tests unitaires** : Tous les endpoints fonctionnent correctement

## Ordre d'exécution recommandé

### 1. Projects
1. **Create Project** - Crée un nouveau projet
2. **List Projects** - Liste tous les projets
3. **Get Project** - Récupère les détails d'un projet
4. **Update Project** - Modifie le nom d'un projet
5. **Delete Project** - Supprime un projet

### 2. Entities
1. **Create Entity - User** - Crée une entité User avec attributs polymorphiques
   - IntegerType avec SequentialGenerator (id)
   - IntegerType avec MinMaxConstraint (age)
   - StringType (email)

2. **Create Entity - Product** - Crée une entité Product avec tous les types
   - IntegerType (productId)
   - StringType (name)
   - FloatType avec MinMaxConstraint (price)
   - EnumType (category)
   - BooleanType (inStock)
   - DateType avec MinMaxConstraint (createdAt)

3. **List Entities in Project** - Liste toutes les entités d'un projet
4. **Get Entity** - Récupère les détails d'une entité
5. **Update Entity** - Modifie une entité (ajout d'attributs)
6. **Delete Entity** - Supprime une entité

### 3. Generation & Export
1. **Preview Data** - Génère un aperçu JSON des données
2. **Export JSON** - Télécharge les données en format JSON
3. **Export XML** - Télécharge les données en format XML
4. **Export SQL** - Télécharge les données en format SQL

## Exemples de types polymorphiques

### IntegerType
```json
{
  "typeClass": "IntegerType"
}
```

### FloatType
```json
{
  "typeClass": "FloatType"
}
```

### StringType
```json
{
  "typeClass": "StringType",
  "maxLength": 100
}
```

### BooleanType
```json
{
  "typeClass": "BooleanType"
}
```

### DateType
```json
{
  "typeClass": "DateType"
}
```

### EnumType
```json
{
  "typeClass": "EnumType",
  "values": ["value1", "value2", "value3"]
}
```

## Exemples de contraintes

### MinMaxConstraint
```json
{
  "constraintClass": "MinMaxConstraint",
  "min": 0,
  "max": 100
}
```

### EnumConstraint
```json
{
  "constraintClass": "EnumConstraint",
  "allowedValues": ["val1", "val2"]
}
```

## Exemples de générateurs

### RandomGenerator
```json
{
  "generatorClass": "RandomGenerator"
}
```

### SequentialGenerator
```json
{
  "generatorClass": "SequentialGenerator",
  "start": 1,
  "step": 1
}
```

### EmailGenerator
```json
{
  "generatorClass": "EmailGenerator"
}
```
Génère des emails aléatoires au format prenom.nom@domaine.com (ex: john.smith@gmail.com)

## Notes

- L'application doit être démarrée sur http://localhost:8081
- Après avoir créé un projet, mettez à jour la variable `projectId` dans l'environnement
- Après avoir créé une entité, mettez à jour la variable `entityId` dans l'environnement
- Les dates dans MinMaxConstraint pour DateType sont en millisecondes (epoch timestamp)
  - 1577836800000 = 2020-01-01
  - 1735689600000 = 2025-01-01
