# Dataset Manager Service - Generateur de Datasets

API REST Spring Boot pour la generation automatique de datasets configurables avec export multi-format.

> **Note** : Ce README decrit l'API CRUD de ce service (projets/entites/attributs). Pour la vue d'ensemble de l'architecture microservices (Eureka, Gateway, Config Server, generator-service, Resilience4J), voir le [README principal](../README.md) a la racine du projet.

## Stack Technique

- **Java 25** / **Spring Boot 4.0.1**
- **Spring Data JPA** / Hibernate 7 + **PostgreSQL**, schéma versionné par **Flyway**
- **Lombok** / **Springdoc OpenAPI** (Swagger)
- **Datafaker** pour la generation de donnees realistes
- **Maven** (wrapper inclus)

## Demarrage rapide

```bash
# Cloner et lancer
./mvnw spring-boot:run
```

L'API demarre sur `http://localhost:8081/api`.

Swagger UI : `http://localhost:8081/swagger-ui.html`

## Endpoints de l'API

### Projets

| Methode  | Endpoint              | Description                  |
|----------|-----------------------|------------------------------|
| `POST`   | `/api/projects`       | Creer un projet              |
| `GET`    | `/api/projects`       | Lister tous les projets      |
| `GET`    | `/api/projects/{id}`  | Detail d'un projet           |
| `PUT`    | `/api/projects/{id}`  | Modifier un projet           |
| `DELETE` | `/api/projects/{id}`  | Supprimer un projet          |

### Entites de donnees

| Methode  | Endpoint                                | Description                        |
|----------|-----------------------------------------|------------------------------------|
| `POST`   | `/api/projects/{projectId}/entities`    | Ajouter une entite a un projet     |
| `GET`    | `/api/projects/{projectId}/entities`    | Lister les entites d'un projet     |
| `GET`    | `/api/entities/{id}`                    | Detail d'une entite                |
| `PUT`    | `/api/entities/{id}`                    | Modifier une entite                |
| `DELETE` | `/api/entities/{id}`                    | Supprimer une entite               |

### Generation et Export

| Methode | Endpoint                                  | Description                       |
|---------|-------------------------------------------|-----------------------------------|
| `GET`   | `/api/projects/{id}/preview`              | Apercu des donnees generees       |
| `GET`   | `/api/projects/{id}/export?format=json`   | Export JSON                       |
| `GET`   | `/api/projects/{id}/export?format=xml`    | Export XML                        |
| `GET`   | `/api/projects/{id}/export?format=sql`    | Export SQL                        |
| `GET`   | `/api/projects/{id}/export?format=csv`    | Export CSV                        |

## Architecture

```
src/main/java/com/example/microservice/
  controller/        # Points d'entree REST (ProjectController, DataEntityController, ProjectGenerationController)
  service/           # Logique metier (ProjectService, DataEntityService, DataGenerationService)
  repository/        # Acces donnees JPA
  model/             # Entites JPA (Project, DataEntity, Attribute, Types, Constraints, Generators)
  dto/               # Data Transfer Objects (separation controleur/entite)
    entity/          # DTOs pour les entites
    generation/      # DTOs pour la generation (PreviewResponseDTO, EntityDataDTO, RecordDTO)
    model/           # DTOs pour types, contraintes, generateurs
    project/         # DTOs pour les projets
  mapper/            # Conversion DTO <-> Entity (DataEntityMapper, ProjectMapper)
  export/            # Exporteurs (JSON, XML, SQL, CSV) + ExporterFactory
  generation/        # Generateurs de donnees (FakerDataGenerator, SequentialDataGenerator)
    strategy/        # Strategies Faker par domaine (Identity, Contact, Address, Company, etc.)
  distribution/      # Distributions statistiques (Normal, Uniforme, Aleatoire, Personnalisee)
  source/            # Sources de donnees externes (API, Fichier, ListeStatique)
  dataset/           # Representation interne des donnees generees (Dataset, Record)
  util/              # Classes utilitaires (RecordGenerator)
  config/            # Configuration Spring (CORS, WebConfig)
  validation/        # Validateurs (AttributeValidator)
  exception/         # Gestion globale des erreurs (GlobalExceptionHandler, ErrorResponse)
```

### Principes SOLID appliques

- **SRP** : Services separes (CRUD vs Generation), validation extraite des entites
- **OCP** : ExporterFactory (Map), registres de conversion dans le mapper, strategies Faker
- **DIP** : Injection via interfaces, pas d'instanciation concrete dans les controleurs
- **ISP** : Interfaces ciblees (IDataGenerator, IDataExporter, IDataType, IConstraint)

### Types supportes

| Type          | `typeClass`    | Description            |
|---------------|----------------|------------------------|
| Entier        | `IntegerType`  | Nombres entiers        |
| Flottant      | `FloatType`    | Nombres decimaux       |
| Chaine        | `StringType`   | Texte                  |
| Booleen       | `BooleanType`  | true/false             |
| Date          | `DateType`     | Dates                  |
| Enumeration   | `EnumType`     | Liste de valeurs       |

### Contraintes supportees

| Contrainte       | `constraintClass`   | Proprietes     |
|------------------|---------------------|----------------|
| Min/Max          | `MinMaxConstraint`  | `min`, `max`   |
| Enum             | `EnumConstraint`    | `allowed`      |

### Generateurs supportes

| Generateur    | `generatorClass`       | Description                                     |
|---------------|------------------------|-------------------------------------------------|
| Faker         | `FakerGenerator`       | Donnees realistes via Datafaker (locale: fr/en)  |
| Sequentiel    | `SequentialGenerator`  | Valeurs incrementales (startValue)              |

Le generateur **Faker** detecte automatiquement le domaine de donnees selon le nom de l'attribut via 7 strategies :
- **Identity** : `firstname`, `lastname`, `fullname`, `ssn`, `passport`
- **Contact** : `email`, `phone`, `mobile`
- **Address** : `street`, `city`, `country`, `zipcode`, `state`
- **Company** : `company`, `industry`, `jobTitle`
- **Internet** : `url`, `domain`, `text`, `paragraph`, `word`
- **Commerce** : `price`, `product`, `department`

### Distributions supportees (module `distribution/`)

| Distribution | Description |
|--------------|-------------|
| Normal       | Distribution gaussienne (moyenne, ecart-type) |
| Uniforme     | Distribution uniforme (tous les nombres equiprobables) |
| Aleatoire    | Distribution aleatoire simple |
| Personnalisee| Distribution personnalisable |

### Sources de donnees supportees (module `source/`)

| Source | Description |
|--------|-------------|
| API    | Recuperation depuis une API REST externe |
| Fichier| Lecture depuis un fichier (CSV, JSON, etc.) |
| ListeStatique | Utilisation d'une liste predéfinie |

## Exemples de payload

### Exemple 1 : Entite simple

```json
POST /api/projects/1/entities
{
  "name": "Utilisateur",
  "quantity": 100,
  "attributes": [
    {
      "name": "id",
      "type": { "typeClass": "IntegerType" },
      "generator": { "generatorClass": "SequentialGenerator", "startValue": 1 }
    },
    {
      "name": "fullName",
      "type": { "typeClass": "StringType" },
      "generator": { "generatorClass": "FakerGenerator", "locale": "fr" }
    },
    {
      "name": "age",
      "type": { "typeClass": "IntegerType" },
      "constraints": [{ "constraintClass": "MinMaxConstraint", "min": 18, "max": 90 }]
    },
    {
      "name": "email",
      "type": { "typeClass": "StringType" },
      "generator": { "generatorClass": "FakerGenerator" }
    },
    {
      "name": "role",
      "type": { "typeClass": "EnumType", "values": ["ADMIN", "EDITOR", "VIEWER"] }
    }
  ]
}
```

### Exemple 2 : Entite avec sous-entites (relation one-to-many)

```json
POST /api/projects/1/entities
{
  "name": "Client",
  "quantity": 50,
  "attributes": [
    {
      "name": "clientId",
      "type": { "typeClass": "IntegerType" },
      "generator": { "generatorClass": "SequentialGenerator", "startValue": 1000 }
    },
    {
      "name": "companyName",
      "type": { "typeClass": "StringType" },
      "generator": { "generatorClass": "FakerGenerator" }
    }
  ],
  "subEntities": [
    {
      "name": "Commande",
      "quantity": 3,
      "attributes": [
        {
          "name": "orderId",
          "type": { "typeClass": "IntegerType" },
          "generator": { "generatorClass": "SequentialGenerator", "startValue": 1 }
        },
        {
          "name": "amount",
          "type": { "typeClass": "FloatType" },
          "constraints": [{ "constraintClass": "MinMaxConstraint", "min": 10.0, "max": 5000.0 }]
        },
        {
          "name": "orderDate",
          "type": { "typeClass": "DateType" }
        }
      ]
    }
  ]
}
```
Ce payload genere 50 clients, chacun avec 3 commandes (total: 150 commandes).

## Gestion des erreurs

| Code | Situation                                      |
|------|-------------------------------------------------|
| 400  | JSON invalide, validation echouee, type inconnu |
| 404  | Ressource non trouvee                           |
| 409  | Violation de contrainte d'unicite (doublon)     |
| 500  | Erreur interne non prevue                       |

Contraintes d'unicite :
- `Project.name` : unique
- `DataEntity(project_id, name)` : unique par projet
- `Attribute(data_entity_id, name)` : unique par entite

## Tests & QA

Les tests sont organises en 3 dossiers avec **Bruno** (GUI + CLI).

### Structure des tests

```
tests/bruno-collection/
  environments/MicroService.bru     # Variables d'environnement
  UnitTests/                        # Dossier A : 13 tests unitaires (cas passants)
  ErrorTests/                       # Dossier B : 7 tests d'erreur (400, 404, 409)
  IntegrationTests/                 # Dossier C : 6 tests integration (scenario nominal)
```

### Dossier A - Tests Unitaires (13 tests)

Tests isoles verifiant chaque endpoint avec des donnees valides :
- **Projets** : ListProjects, CreateProject, GetProject, UpdateProject, DeleteProject
- **Entites** : CreateEntity, ListEntities, GetEntity, UpdateEntity, DeleteEntity
- **Generation** : PreviewData, ExportJSON, ExportXML (+ ExportCSV et ExportSQL disponibles)

### Dossier B - Tests d'Erreur (7 tests)

| Test                                  | Code attendu |
|---------------------------------------|--------------|
| Create Project Without Name           | 400          |
| Create Entity Invalid Constraint      | 400          |
| Create Entity Unknown Type            | 400          |
| Get Project Not Found                 | 404          |
| Get Entity Not Found                  | 404          |
| Delete Project Not Found              | 404          |
| Create Duplicate Project              | 409          |

### Dossier C - Tests d'Integration (6 tests - Scenario Nominal)

Scenario complet chainant les requetes via variables d'environnement (`current_project_id`, `current_entity_id`). Idempotent : rejouable a l'infini grace au nettoyage final.

1. CreateProject -> stocke `current_project_id`
2. AddEntity -> stocke `current_entity_id`
3. ModifyEntity
4. PreviewData
5. ExportJSON
6. CleanupDeleteProject

### Installation et execution CLI

```bash
# Installer Bruno CLI
npm install -g @usebruno/cli

# Lancer les tests d'integration
cd tests/bruno-collection
bru run IntegrationTests --env MicroService

# Lancer les tests unitaires
bru run UnitTests --env MicroService

# Lancer les tests d'erreur
bru run ErrorTests --env MicroService

# Generer un rapport d'execution
bru run IntegrationTests --env MicroService > report-cli.txt
```

### Rapport d'execution

Le fichier `tests/bruno-collection/report-cli.txt` contient le dernier rapport :
- 6/6 requetes passees
- 27/27 assertions reussies

## Auteurs

- Med Amine & Mamitiana - MBDS 2025/2026
