# Bruno Collection - Spring REST Microservice API

Collection de tests pour l'API REST du microservice de génération de données.

## Configuration

### Variables d'environnement (Local)
- `baseUrl`: http://localhost:8081/api
- `projectId`: 1 (à modifier après création d'un projet)
- `entityId`: 1 (à modifier après création d'une entité)

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
