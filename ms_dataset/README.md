# Générateur de Datasets — Architecture Microservices

Ce dépôt contient la version "découpée en microservices" de notre générateur de datasets. La V1 était un monolithe Spring Boot qui faisait tout : gestion des projets, des entités, et génération des données. Le problème, c'est que dès qu'on demandait un gros volume de lignes (genre 1 million), la génération bouffait tout le CPU et rendait l'API de gestion des projets quasi inutilisable pendant ce temps-là.

Le but de cette étape était donc de sortir toute la partie "génération" dans un service à part, stateless, qu'on peut scaler indépendamment, et de mettre en place l'infrastructure qui va autour (annuaire de services, gateway, configuration centralisée, tolérance aux pannes).

## Architecture

On est passé de 1 à 5 modules :

```
                         ┌──────────────────────┐
                         │  Config Service       │  :8888
                         │  (Spring Cloud Config) │
                         └──────────▲────────────┘
                                     │ config
                         ┌──────────┴────────────┐
                         │  Discovery Server      │  :8761
                         │  (Eureka)              │
                         └──────────▲────────────┘
                                     │ enregistrement
              ┌──────────────┬──────┴───────┬───────────────┐
              │              │               │
        ┌─────▼─────┐  ┌─────▼──────┐  ┌────▼───────────────┐
client →│  Gateway   │→ │  dataset-  │  │  generator-service  │
        │  :8083     │  │  manager   │←─┤  :8082 (stateless)  │
        └────────────┘  │  :8081     │  │  Feign + Resilience4J│
                         └────────────┘  └─────────────────────┘
```

| Module | Rôle | Port | Base de données |
|---|---|---|---|
| `discovery_server` | Annuaire Eureka | 8761 | — |
| `config_service` | Config Server (mode `native`, lit `config-repo/`) | 8888 | — |
| `gateway` | Point d'entrée unique (Spring Cloud Gateway) | 8083 | — |
| `dataset_manager_service` | Gestion des projets / entités / attributs (CRUD) | 8081 | H2 fichier |
| `generator_service` | Génération + export des données (worker stateless) | 8082 | aucune |

La configuration partagée (ports, routes de la gateway, paramètres Resilience4J, URL Eureka...) est centralisée dans `config-repo/`. Chaque service va la chercher au démarrage auprès du `config_service`.

## Stack technique

- Java 25 / Spring Boot 4.0.x / Spring Cloud 2025.1.1
- Spring Cloud Netflix Eureka (Discovery)
- Spring Cloud Gateway (WebFlux)
- Spring Cloud Config Server (profil `native`)
- OpenFeign pour les appels inter-services
- Resilience4J (Circuit Breaker + TimeLimiter)
- Spring Data JPA + H2 (uniquement côté `dataset_manager_service`)
- Datafaker pour la génération de données réalistes
- Lombok, MapStruct, Springdoc OpenAPI

## Lancer le projet

**L'ordre de démarrage compte**, parce que la plupart des services vont chercher leur config (port, routes, paramètres Eureka/Resilience4J...) auprès du Config Server au démarrage. Si le Config Server n'est pas encore up, l'import est `optional:` donc ça ne plante pas, mais le service démarre alors avec une config minimale (port par défaut 8080 etc.).

Ordre conseillé :

1. **`config_service`** (8888)
2. **`discovery_server`** (8761)
3. **`dataset_manager_service`** (8081)
4. **`generator_service`** (8082)
5. **`gateway`** (8083)

Pour chaque module, depuis son dossier :

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

Une fois tout démarré, le dashboard Eureka (http://localhost:8761) doit afficher 4 instances enregistrées : `DATASET-MANAGER`, `GENERATOR`, `GATEWAY` et `CONFIG-SERVICE` (le Config Server s'enregistre aussi via son client Eureka).

## Détail des services

### 1. `discovery_server` (Eureka)

Serveur Eureka classique, configuré pour ne pas s'enregistrer lui-même (`register-with-eureka=false`, `fetch-registry=false`). Dashboard : http://localhost:8761

### 2. `config_service` (Spring Cloud Config)

Sert les fichiers de `config-repo/` en mode `native`. Le chemin est calculé dynamiquement (`${user.dir}/../config-repo/`) pour rester portable entre Windows et macOS — il suffit de lancer le service depuis son propre dossier (`config_service/`), ce qui est le comportement par défaut de Maven et des IDE.

On peut vérifier que la config d'un service est bien servie en interrogeant directement, par exemple :

```
http://localhost:8888/generator/default
http://localhost:8888/gateway/default
```

### 3. `gateway` (Spring Cloud Gateway)

Point d'entrée unique sur le port **8083**. Deux types de routes sont actives :

- Les **routes manuelles** définies dans `config-repo/gateway.properties` :
  - `/DATASET-MANAGER/api/projects/**` → `dataset-manager-service`
  - `/GENERATOR-SERVICE/api/generator/**` → `generator-service`
- Les **routes automatiques** générées par `discovery.locator` (activé), basées sur le nom Eureka en minuscule, ex. `/dataset-manager/**` → `dataset-manager`, `/generator/**` → `generator`. C'est ce qu'on utilise dans les exemples ci-dessous, le temps de finir de valider la réécriture des routes manuelles (cf. section Limites).

### 4. `dataset_manager_service`

C'est l'ancien monolithe, allégé de toute la logique de génération. Il garde le CRUD complet sur les projets et leurs entités/attributs, persistés en H2 (fichier dans `dataset_manager_service/data/devDb`).

Context-path : `/api`. Endpoints principaux :

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/projects` | Créer un projet |
| GET | `/api/projects` | Lister les projets |
| GET | `/api/projects/{id}` | Détail d'un projet |
| PUT | `/api/projects/{id}` | Modifier un projet |
| DELETE | `/api/projects/{id}` | Supprimer un projet |
| POST | `/api/projects/{projectId}/entities` | Ajouter une entité |
| GET | `/api/projects/{projectId}/entities` | Lister les entités d'un projet |
| GET | `/api/entities/{id}` | Détail d'une entité |
| PUT | `/api/entities/{id}` | Modifier une entité |
| DELETE | `/api/entities/{id}` | Supprimer une entité |

Swagger UI : http://localhost:8081/api/swagger-ui/index.html
Console H2 : http://localhost:8081/api/h2-console

Pour le détail des types/contraintes supportés et des exemples de payload complets (entités avec sous-entités, etc.), voir `dataset_manager_service/readme.md`.

### 5. `generator_service`

Le nouveau service "ouvrier". **Stateless** (pas de base de données, pas d'état en mémoire entre deux requêtes), donc on peut en lancer plusieurs instances derrière Eureka sans souci de cohérence.

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/api/generator/{projectId}/preview` | Aperçu JSON des données générées |
| GET | `/api/generator/{projectId}/export?format=json\|xml\|csv\|sql` | Export du fichier complet |

Pour générer les données, ce service ne connaît rien du modèle : il va chercher la définition du projet (entités, attributs, types, contraintes) auprès de `dataset-manager` via Feign, puis génère les valeurs avec Datafaker. Le générateur détecte automatiquement le domaine d'un attribut à partir de son nom (`email`, `firstName`, `city`, `price`...) et, à défaut, se base sur son type (`IntegerType`, `DateType`, `EnumType`...).

## Communication entre services — OpenFeign

`generator-service` appelle `dataset-manager` via l'interface `DatasetManagerClient` :

```java
@FeignClient(name = "dataset-manager", fallback = DatasetManagerClientFallback.class)
public interface DatasetManagerClient {

    @GetMapping("/api/projects/{projectId}")
    ProjectResponseDTO getProject(@PathVariable Long projectId);

    @GetMapping("/api/projects/{projectId}/entities")
    List<DataEntityResponseDTO> getProjectEntities(@PathVariable Long projectId);
}
```

Le nom `dataset-manager` correspond au `spring.application.name` enregistré dans Eureka — Feign + Ribbon/LoadBalancer se chargent de résoudre l'adresse réelle.

## Résilience — Resilience4J

L'appel Feign vers `dataset-manager` est protégé par un Circuit Breaker et un TimeLimiter, configurés dans `config-repo/generator.properties` :

```properties
resilience4j.circuitbreaker.instances.datasetManagerClient.slidingWindowSize=10
resilience4j.circuitbreaker.instances.datasetManagerClient.minimumNumberOfCalls=5
resilience4j.circuitbreaker.instances.datasetManagerClient.failureRateThreshold=50
resilience4j.circuitbreaker.instances.datasetManagerClient.waitDurationInOpenState=5s
resilience4j.circuitbreaker.instances.datasetManagerClient.permittedNumberOfCallsInHalfOpenState=3
resilience4j.circuitbreaker.instances.datasetManagerClient.automaticTransitionFromOpenToHalfOpenEnabled=true

resilience4j.timelimiter.instances.datasetManagerClient.timeoutDuration=10s
resilience4j.timelimiter.instances.datasetManagerClient.cancelRunningFuture=true
```

En clair :
- au bout de 5 appels minimum, si plus de 50% ont échoué sur les 10 derniers, le circuit s'ouvre ;
- pendant 5 secondes, on ne tente même plus d'appeler `dataset-manager` (échec immédiat → fallback) ;
- si un appel met plus de 10 secondes, il est annulé et traité comme un échec.

Le **fallback** (`DatasetManagerClientFallback`) renvoie `null` pour `getProject` et une liste vide pour `getProjectEntities`. Côté `DataGenerationServiceImpl`, si `dataset-manager` ne répond pas, l'endpoint `/preview` renvoie quand même un **200 OK** avec un corps du type :

```json
{
  "projectId": 1,
  "projectName": "Not available",
  "status": "PARTIAL",
  "message": "Le service dataset-manager est indisponible. Veuillez réessayer plus tard.",
  "data": []
}
```

→ Pas de stacktrace renvoyée au client, conformément à la consigne.

## Scénario de test

1. Démarrer les 5 services dans l'ordre indiqué plus haut.
2. Créer un projet et une entité sur `dataset-manager` :

```bash
curl -X POST http://localhost:8081/api/projects \
  -H "Content-Type: application/json" \
  -d '{"name": "Demo"}'

curl -X POST http://localhost:8081/api/projects/1/entities \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Utilisateur",
    "quantity": 5,
    "attributes": [
      {"name": "fullName", "type": {"typeClass": "StringType"}},
      {"name": "email", "type": {"typeClass": "StringType"}},
      {"name": "age", "type": {"typeClass": "IntegerType"}, "constraints": [{"constraintClass": "MinMaxConstraint", "min": 18, "max": 65}]}
    ]
  }'
```

3. Demander une génération via `generator-service` (en direct ou via la gateway) :

```bash
curl http://localhost:8082/api/generator/1/preview
# ou via la gateway (route auto-découverte) :
curl http://localhost:8083/generator/api/generator/1/preview
```

→ Réponse `200 OK` avec `status: "OK"` et les 5 lignes générées pour `Utilisateur`.

4. **Couper brutalement `dataset_manager_service`** (Ctrl+C dans son terminal).
5. Refaire la même requête de preview.

→ Réponse toujours `200 OK`, mais avec `status: "PARTIAL"` et `data: []` : le générateur ne plante pas, le Circuit Breaker + Fallback prennent le relais.

## Tests

Les tests fonctionnels (collection Bruno) couvrent le CRUD de `dataset_manager_service` (tests unitaires, tests d'erreur, scénario d'intégration). Voir `dataset_manager_service/tests/bruno-collection/README.md` pour les lancer.

## Limites connues / à finaliser

- La règle de réécriture de la gateway pour `/DATASET-MANAGER/api/projects/**` est à revérifier vis-à-vis du `context-path=/api` de `dataset-manager` (les routes auto-découverte `/dataset-manager/**` et `/generator/**` fonctionnent en attendant).
- Le champ `generator` (FakerGenerator/SequentialGenerator, locale, valeur de départ...) défini sur un attribut n'est pas encore propagé jusqu'au `generator-service` : la génération se base uniquement sur le nom et le type de l'attribut pour l'instant.
- Les distributions statistiques (`distribution/` côté `dataset-manager`) ne sont pas encore exploitées par le `generator-service`.
- Dockerisation de l'ensemble : pas encore faite (optionnel dans l'énoncé).

## Auteurs

Med Amine & Mamitiana - MBDS 2025/2026