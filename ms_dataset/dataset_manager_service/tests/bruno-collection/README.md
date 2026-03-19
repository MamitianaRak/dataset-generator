# Bruno Collection - Tests API REST

Collection de tests Bruno pour le microservice de generation de datasets.

## Configuration

### Environnement : MicroService

| Variable             | Valeur                        | Description                       |
|----------------------|-------------------------------|-----------------------------------|
| `baseUrl`            | `http://localhost:8081/api`   | URL de base de l'API              |
| `projectId`          | `1`                           | ID projet pour tests unitaires    |
| `entityId`           | `1`                           | ID entite pour tests unitaires    |
| `current_project_id` | (dynamique)                   | Defini par IntegrationTests       |
| `current_entity_id`  | (dynamique)                   | Defini par IntegrationTests       |

## Structure des tests

### Dossier A - UnitTests (13 tests)

Tests isoles sur chaque endpoint avec des donnees valides.

| # | Test           | Methode | Endpoint                                   |
|---|----------------|---------|---------------------------------------------|
| 1 | ListProjects   | GET     | `/projects`                                 |
| 2 | GetProject     | GET     | `/projects/{projectId}`                     |
| 3 | CreateProject  | POST    | `/projects`                                 |
| 4 | UpdateProject  | PUT     | `/projects/{projectId}`                     |
| 5 | DeleteProject  | DELETE  | `/projects/{projectId}`                     |
| 6 | ListEntities   | GET     | `/projects/{projectId}/entities`            |
| 7 | GetEntity      | GET     | `/entities/{entityId}`                      |
| 8 | CreateEntity   | POST    | `/projects/{projectId}/entities`            |
| 9 | UpdateEntity   | PUT     | `/entities/{entityId}`                      |
| 10| DeleteEntity   | DELETE  | `/entities/{entityId}`                      |
| 11| PreviewData    | GET     | `/projects/{projectId}/preview`             |
| 12| ExportJSON     | GET     | `/projects/{projectId}/export?format=json`  |
| 13| ExportXML      | GET     | `/projects/{projectId}/export?format=xml`   |

### Dossier B - ErrorTests (7 tests)

Tests de gestion des erreurs (pas de 500 pour des erreurs client).

| Test                                  | Code | Description                          |
|---------------------------------------|------|--------------------------------------|
| 400 - Create Project Without Name     | 400  | POST projet sans nom                 |
| 400 - Create Entity Invalid Constraint| 400  | min > max dans MinMaxConstraint      |
| 400 - Create Entity Unknown Type      | 400  | typeClass inconnu                    |
| 404 - Get Project Not Found           | 404  | GET projet inexistant (id=999999)    |
| 404 - Get Entity Not Found            | 404  | GET entite inexistante (id=999999)   |
| 404 - Delete Project Not Found        | 404  | DELETE projet inexistant             |
| 409 - Create Duplicate Project        | 409  | POST projet avec nom deja existant   |

### Dossier C - IntegrationTests (6 tests - Scenario Nominal)

Scenario complet "Happy Path" avec chainage de variables. Idempotent (rejouable a l'infini).

| Seq | Test                 | Description                                          |
|-----|----------------------|------------------------------------------------------|
| 1   | CreateProject        | Cree un projet (nom unique avec timestamp)           |
| 2   | AddEntity            | Ajoute une entite avec 3 attributs + FakerGenerator  |
| 3   | ModifyEntity         | Modifie l'entite (5 attributs, quantite 25)          |
| 4   | PreviewData          | Verifie la generation des donnees                    |
| 5   | ExportJSON           | Telecharge l'export JSON                             |
| 6   | CleanupDeleteProject | Supprime le projet (nettoyage pour idempotence)      |

## Types polymorphiques (JSON)

### Types (`typeClass`)
```json
{ "typeClass": "IntegerType" }
{ "typeClass": "FloatType" }
{ "typeClass": "StringType" }
{ "typeClass": "BooleanType" }
{ "typeClass": "DateType" }
{ "typeClass": "EnumType", "values": ["val1", "val2"] }
```

### Contraintes (`constraintClass`)
```json
{ "constraintClass": "MinMaxConstraint", "min": 0, "max": 100 }
{ "constraintClass": "EnumConstraint", "allowed": ["val1", "val2"] }
```

### Generateurs (`generatorClass`)
```json
{ "generatorClass": "FakerGenerator", "locale": "fr" }
{ "generatorClass": "SequentialGenerator", "startValue": 1 }
```

## Execution CLI

### Prerequis
- Backend Spring demarre sur `http://localhost:8081`
- Bruno CLI installe : `npm install -g @usebruno/cli`

### Commandes

```bash
cd tests/bruno-collection

# Tests d'integration (scenario nominal)
bru run IntegrationTests --env MicroService

# Tests unitaires
bru run UnitTests --env MicroService

# Tests d'erreur
bru run ErrorTests --env MicroService

# Tous les tests + rapport et pour l'affichage UTF-8 
$env:JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"; bru run IntegrationTests --env MicroService --sandbox=developer | Out-File -FilePath .\report-cli.txt -Encoding utf8      
```

### Dernier rapport

Fichier : `report-cli.txt`
- 6/6 requetes passees
- 27/27 assertions reussies
