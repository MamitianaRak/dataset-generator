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
| `dataset_manager_service` | Gestion des projets / entités / attributs (CRUD) | 8081 | H2 fichier (schéma Flyway) |
| `generator_service` | Génération + export des données (worker stateless) | 8082 | aucune |

La configuration partagée (ports, routes de la gateway, paramètres Resilience4J, URL Eureka...) est centralisée dans `config-repo/`. Chaque service va la chercher au démarrage auprès du `config_service`.

## Stack technique

- Java 25 / Spring Boot 4.0.x / Spring Cloud 2025.1.1
- Spring Cloud Netflix Eureka (Discovery)
- Spring Cloud Gateway (WebFlux)
- Spring Cloud Config Server (profil `native`)
- OpenFeign pour les appels inter-services
- Resilience4J (Circuit Breaker + TimeLimiter)
- Spring Data JPA / Hibernate 7, H2, Flyway (uniquement côté `dataset_manager_service`)
- Tests : JUnit 5 ; Testcontainers (PostgreSQL) pour vérifier que l'export SQL se charge dans une vraie base
- Datafaker pour la génération de données réalistes
- Lombok, MapStruct, Springdoc OpenAPI

## Lancer le projet

### Option 1 : Avec Docker (recommandé) 🐳

**Avantage :** Démarrage automatique de tous les services en une seule commande, aucune installation Java/Maven requise.

```bash
docker-compose up --build
```

Voir la section **Dockerisation** plus bas pour le guide complet.

### Option 2 : Manuel (développement)

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

C'est l'ancien monolithe, allégé de toute la logique de génération. Il garde le CRUD complet sur les projets et leurs entités/attributs, persistés en H2 (fichier `data/dataset-manager`, voir « Couche de persistance » plus bas).

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

Pour le détail des types/contraintes supportés et des exemples de payload complets (entités avec sous-entités, etc.), voir `dataset_manager_service/readme.md`.

#### Couche de persistance

La couche JPA a été reprise après mesure : le service générateur charge un projet complet avant chaque aperçu et chaque export, et ce chargement déclenchait une requête par entité, par attribut et par type (problème N+1).

| Projet de test : 5 entités × 6 attributs, plus 5 sous-entités × 3 | Avant | Après |
|---|---|---|
| Requêtes SQL pour charger le projet | 87 | 2 |
| Requêtes SQL pour le créer | 210 | 29 |

Mesuré avec les statistiques Hibernate (`ProjectLoadingQueryCountTest`, sur H2). Le test échoue si le chargement repasse au-dessus de 2 requêtes.

Ce qui a changé :

- **Type et contraintes d'un attribut dans une colonne JSON.** Ce sont des valeurs propres à un attribut, sans identité partagée : elles étaient des entités dans trois tables (avec héritage, `@ManyToMany` et `CascadeType.ALL`), chargées en `EAGER` attribut par attribut. Elles sont maintenant une colonne lue avec la ligne.
- **Un projet en une requête.** Les sous-entités portent aussi leur `project_id` ; le projet est chargé par un `join fetch` sur les attributs, puis l'arbre est reconstruit en mémoire à partir des `parent_id`. Les noms d'entités sont uniques par projet, sous-entités comprises (ils deviennent des noms de tables à l'export SQL).
- **Insertions groupées.** Les identifiants viennent de séquences allouées par blocs de 50 au lieu de colonnes `IDENTITY`, ce qui permet à Hibernate d'envoyer les `INSERT` en lots JDBC.
- **Mise à jour sans tout recréer.** Les attributs sont rapprochés par nom : un attribut conservé garde son identifiant, au lieu d'être supprimé puis réinséré (contournement qui imposait un `flush` intermédiaire, Hibernate exécutant les insertions avant les suppressions).
- **Schéma versionné.** Flyway crée le schéma (`db/migration`), Hibernate le valide (`ddl-auto=validate`) au lieu de le modifier (`update`). Les suppressions en cascade sont faites par la base (`on delete cascade`).
- **Entités correctes.** Verrou optimiste (`@Version`, conflit renvoyé en 409), `equals`/`hashCode` sur l'identifiant sans casser les collections avant persistance, plus de `@Builder` Lombok qui contournait la synchronisation des relations, `open-in-view` désactivé.
- **Code mort retiré** : distributions, sources de données et logique de génération restées dans les entités alors que la génération est faite par `generator_service`.

Les tests (`mvn test`) tournent sur H2 en mémoire, sans Docker. Console H2 : http://localhost:8081/api/h2-console (URL JDBC `jdbc:h2:file:./data/dataset-manager`, utilisateur `sa`). Une ancienne base `data/devDb`, créée par `ddl-auto=update`, n'est plus utilisée : Flyway refuse de migrer un schéma qu'il n'a pas créé.

### 5. `generator_service`

Le nouveau service "ouvrier". **Stateless** (pas de base de données, pas d'état en mémoire entre deux requêtes), donc on peut en lancer plusieurs instances derrière Eureka sans souci de cohérence.

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/api/generator/{projectId}/preview` | Aperçu JSON : 100 lignes par table au plus (`app.preview.rows-per-table`) |
| GET | `/api/generator/{projectId}/export?format=json\|xml\|csv\|sql` | Export complet, envoyé en streaming |

Pour générer les données, ce service ne connaît rien du modèle : il va chercher la définition du projet (entités, attributs, types, contraintes) auprès de `dataset-manager` via Feign, puis génère les valeurs avec Datafaker. Le générateur détecte automatiquement le domaine d'un attribut à partir de son nom (`email`, `firstName`, `city`, `price`...) et, à défaut, se base sur son type (`IntegerType`, `DateType`, `EnumType`...).

#### Données relationnelles

Chaque table reçoit une clé `id` (1 à n). Une sous-entité reçoit une colonne `<parent>_id` qui contient l'`id` d'une ligne existante de sa table parente : les tables générées sont reliées comme dans une vraie base. Les parents sont générés avant leurs sous-entités, et les exports les écrivent dans cet ordre.

L'export SQL commence chaque table par un `CREATE TABLE` avec sa clé primaire et sa clé étrangère, puis insère les lignes : le script se charge tel quel dans PostgreSQL, contraintes actives. Le type de chaque colonne est déduit des valeurs réellement générées, pas du type déclaré, car le générateur par nom peut renvoyer du texte pour un attribut déclaré entier (`phone`, par exemple). `SqlExportLoadsIntoPostgresTest` charge un export client → commande → ligne de commande (200, 1 000 et 3 000 lignes, dont une table nommée `order`, mot réservé) dans un PostgreSQL réel et vérifie les jointures.

#### Export en streaming

L'export écrit chaque ligne dans la réponse HTTP dès qu'elle est générée (`StreamingResponseBody`), sans fichier temporaire ni jeu de données en mémoire : seuls le nombre de lignes de chaque table parente et un échantillon de 1 000 lignes servant à fixer les types des colonnes sont gardés. Avant, tout le jeu de données était construit en mémoire puis écrit dans un dossier temporaire jamais supprimé.

| 1 000 000 de lignes exportées en SQL, JVM limitée à 64 Mo de heap | Résultat |
|---|---|
| Génération en mémoire puis export (fonctionnement précédent) | `OutOfMemoryError` |
| Export en streaming | terminé |

Mesuré par `StreamingMemoryTest`, qui lance chaque cas dans une JVM séparée avec `-Xmx64m`. Le nombre de lignes par table est plafonné à 1 000 000 côté `dataset-manager` : le plafond borne la durée et la taille d'un export, plus la mémoire du serveur.

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
- si un appel met plus de 10 secondes, il est annulé et traité comme un échec ;
- un 404 (projet inexistant) n'est pas une panne : il ne compte pas dans le taux d'échec (`ClientErrorPredicate`).

**Nom du circuit breaker.** Par défaut, Spring Cloud OpenFeign crée un circuit breaker par méthode, nommé d'après elle (`DatasetManagerClientgetProjectEntitiesLong`). La configuration ci-dessus, qui vise `datasetManagerClient`, ne s'appliquait donc à rien : les valeurs par défaut tournaient, dont un TimeLimiter de **1 seconde**. Un premier appel lent mais sain partait ainsi dans le fallback, et l'export renvoyait un fichier vide. `FeignResilienceConfig` nomme désormais le circuit breaker d'après l'interface du client, et `DatasetManagerOutageTest` vérifie qu'un seul circuit breaker existe, avec ces réglages.

Le **fallback** (`DatasetManagerClientFallback`, une `FallbackFactory`) reçoit la cause de l'échec. Il ne renvoie jamais de données vides : un 404 de `dataset-manager` devient un 404 pour le client, une panne devient une `DatasetManagerUnavailableException`. Ensuite :

- l'**export** répond **503** avec `{"status": "PARTIAL", "message": "...", "data": []}`, pour qu'un script qui télécharge un export ne prenne pas une panne pour un jeu de données vide ;
- l'**aperçu** répond **200 OK** avec un corps du type :

```json
{
  "projectId": 1,
  "projectName": "Not available",
  "status": "PARTIAL",
  "message": "Le service dataset-manager est indisponible. Veuillez réessayer plus tard.",
  "data": []
}
```

→ Pas de stacktrace renvoyée au client, conformément à la consigne. Un format d'export inconnu répond 400 au lieu de 500 (`GeneratorExceptionHandler`).

`DatasetManagerOutageTest` rejoue le scénario de validation contre un faux `dataset-manager` (serveur HTTP du JDK), avec le vrai fichier `config-repo/generator.properties` : export sain, manager lent (2 s) mais en bonne santé, projet inexistant (404, circuit toujours fermé), panne (503 et fallback `PARTIAL`), ouverture du circuit après 5 échecs, format inconnu (400).

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

## Dockerisation

### Prérequis

- **Docker Desktop** installé et démarré (inclut Docker Compose)
- Aucune autre installation requise (pas besoin de Java, Maven ou Node.js)

```bash
docker --version
docker-compose --version
```

### Démarrage

```bash
# Build et démarrer tous les services (première fois ~5-10 min)
docker-compose up --build

# Mode détaché (arrière-plan)
docker-compose up -d --build

# Démarrer sans rebuild
docker-compose up -d
```

Les services démarrent dans le bon ordre grâce aux healthchecks : Config → Eureka → Manager + Generator → Gateway → Frontend.

### Vérifier que tout fonctionne

```bash
docker-compose ps
```

6 conteneurs doivent être en "Up" : `config-service` (8888), `discovery-server` (8761), `dataset-manager` (8081), `generator-service` (8082), `gateway` (8083), `frontend` (5173).

Ouvrir ensuite :
- Eureka dashboard : http://localhost:8761 → 4 services enregistrés
- Frontend : http://localhost:5173
- Swagger : http://localhost:8081/api/swagger-ui/index.html

### Commandes utiles

```bash
# Logs
docker-compose logs -f                      # tous les services
docker-compose logs -f dataset-manager      # un service

# Arrêt
docker-compose down                         # arrêter
docker-compose down -v                      # arrêter + supprimer la base H2
docker-compose down -v --rmi all            # tout supprimer

# Rebuild d'un seul service
docker-compose up -d --build dataset-manager

# Accéder à un conteneur
docker exec -it dataset-manager sh
```

### Persistance des données

La base H2 est stockée dans le volume Docker `dataset-manager-data`, géré automatiquement. Elle survit aux redémarrages (`docker-compose down`) mais est effacée avec `docker-compose down -v`. Le schéma est créé par Flyway au premier démarrage de `dataset-manager`.

### Dépannage

**Un service ne démarre pas** → `docker-compose logs -f <service>`

**Port déjà utilisé** → `lsof -i :8081` pour identifier le process, ou changer le port dans `docker-compose.yml`

**Build échoue** →
```bash
docker-compose down --rmi all
docker-compose up --build
```

**Services absents dans Eureka** → Attendre 30-60 secondes après le démarrage, le temps que les services s'enregistrent.

## Limites connues / à finaliser

- La réécriture des routes manuelles de la gateway (`/DATASET-MANAGER/...`, `/GENERATOR-SERVICE/...`) a été corrigée : elle retire seulement le préfixe. L'ancienne règle perdait le segment `projects`. Elle reste à valider en lançant la gateway.
- Le champ `generator` (FakerGenerator/SequentialGenerator, locale, valeur de départ...) est accepté par l'API mais n'est ni enregistré ni utilisé : la génération se base sur le nom et le type de l'attribut.
- Les distributions statistiques (loi normale, probabilités par valeur d'une énumération) ne sont pas gérées.

## Auteurs

Med Amine & Mamitiana - MBDS 2025/2026