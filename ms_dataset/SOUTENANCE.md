# Notes pour la soutenance du 12/06

Objectif : 10 min de présentation (architecture + démo) puis questions/code en live. Ce fichier sert de pense-bête, pas à lire mot à mot devant le prof.

---

## 1. Le pitch en 30 secondes

> "On avait un monolithe Spring Boot qui gérait à la fois les projets/entités ET la génération de données. Le problème : générer beaucoup de lignes (ex: 1M) bouffait tout le CPU et bloquait le reste de l'API pendant ce temps. On a donc découpé en 5 services : un annuaire (Eureka), une passerelle (Gateway), un serveur de config centralisé, le service de gestion (l'ancien monolithe allégé) et un nouveau service de génération, stateless, qu'on peut dupliquer à volonté. Les deux services métier communiquent en HTTP via OpenFeign, et l'appel est protégé par un Circuit Breaker Resilience4J avec un fallback propre si le service de gestion tombe."

C'est globalement la trame de tes 10 minutes : **pourquoi** (contexte) → **quoi** (les 5 services) → **comment** (Feign + résilience) → **démo**.

---

## 2. Vocabulaire à avoir en tête

- **Microservice** : une appli Spring Boot indépendante, avec son propre process/port, qui fait *une* chose.
- **Service Discovery (Eureka)** : un annuaire. Chaque service s'enregistre au démarrage avec son nom (`spring.application.name`). Les autres le retrouvent par ce nom au lieu d'une IP/port en dur. Permet d'avoir plusieurs instances du même service (load balancing).
- **API Gateway** : le point d'entrée unique. Le client (Postman/front) ne parle qu'à la gateway, qui route vers le bon service selon l'URL.
- **Config Server** : un service qui sert des fichiers `.properties`/`.yml` centralisés (`config-repo/`). Les autres services les téléchargent au démarrage au lieu de les avoir en dur dans leur jar.
- **OpenFeign** : un client HTTP déclaratif. On écrit une interface Java avec des annotations `@GetMapping`, et Spring génère l'implémentation qui fait l'appel HTTP vers un autre service (résolu via Eureka).
- **Stateless** : le service ne garde aucune donnée en mémoire entre deux requêtes. Donc on peut en lancer 2, 3, 10 instances derrière la gateway sans souci de cohérence.
- **Circuit Breaker** : un disjoncteur électrique mais pour les appels réseau. S'il y a trop d'échecs, il "ouvre le circuit" et arrête d'appeler le service en panne pendant un moment, pour ne pas le surcharger et pour répondre vite (fallback) au lieu d'attendre un timeout à chaque fois.
- **TimeLimiter** : si un appel met trop de temps, on l'annule et on considère ça comme un échec.
- **Fallback** : la réponse de secours renvoyée quand l'appel normal échoue (au lieu de planter / renvoyer une stacktrace).

---

## 3. Architecture - vue d'ensemble

| Service | Port | Rôle | Annotation clé |
|---|---|---|---|
| `discovery_server` | 8761 | Annuaire Eureka | `@EnableEurekaServer` |
| `config_service` | 8888 | Sert `config-repo/*.properties` | `@EnableConfigServer` |
| `gateway` | 8083 | Routage HTTP vers les services | `@EnableDiscoveryClient` |
| `dataset_manager_service` | 8081 | CRUD projets/entités/attributs (H2) | `@EnableDiscoveryClient` |
| `generator_service` | 8082 | Génération + export (stateless) | `@EnableDiscoveryClient` + `@EnableFeignClients` |

**Ordre de démarrage** (important pour la démo) : `config_service` → `discovery_server` → `dataset_manager_service` → `generator_service` → `gateway`.
Pourquoi cet ordre : les autres services font `spring.config.import=optional:configserver:http://localhost:8888` au démarrage pour récupérer leur config (port, routes, paramètres Resilience4J...) depuis `config-repo`.

---

## 4. Les 5 piliers techniques (avec où les montrer dans le code)

### a) Eureka — `discovery_server`

- `discovery_server/src/main/java/.../DiscoveryServerApplication.java` → `@EnableEurekaServer`
- `config-repo/discovery-server.properties` :
  ```properties
  eureka.client.fetch-registry=false
  eureka.client.register-with-eureka=false
  server.port=8761
  ```
  → on précise que le serveur Eureka ne s'enregistre pas lui-même (sinon il essaierait de s'auto-référencer).
- Dashboard à montrer en live : **http://localhost:8761** → on y voit `DATASET-MANAGER`, `GENERATOR`, `GATEWAY`, `CONFIG-SERVICE` enregistrés.

### b) Config Server — `config_service`

- `config_service/src/main/java/.../ConfigServiceApplication.java` → `@EnableConfigServer`
- `config_service/src/main/resources/application.properties` :
  ```properties
  spring.cloud.config.server.native.searchLocations=file:${user.dir}/../config-repo/
  spring.profiles.active=native
  ```
  → mode "native" = il lit des fichiers locaux (par opposition à un repo Git distant). `${user.dir}` rend le chemin portable Windows/Mac.
- `config-repo/` contient un fichier par service (`gateway.properties`, `generator.properties`, etc.), nommé d'après `spring.application.name`.
- Chaque service a `spring.config.import=optional:configserver:http://localhost:8888`. Le `optional:` = si le Config Server n'est pas dispo, le service démarre quand même avec sa config locale minimale.
- À montrer si demandé : `http://localhost:8888/generator/default` renvoie le JSON de la config de `generator`.

### c) Gateway — `gateway`

- `config-repo/gateway.properties` : routes
  ```properties
  spring.cloud.gateway.routes[0].id=dataset-manager-service
  spring.cloud.gateway.routes[0].uri=lb://dataset-manager
  spring.cloud.gateway.routes[0].predicates[0]=Path=/DATASET-MANAGER/api/projects/**
  spring.cloud.gateway.routes[0].filters[0]=RewritePath=...

  spring.cloud.gateway.routes[1].id=generator-service
  spring.cloud.gateway.routes[1].uri=lb://generator
  spring.cloud.gateway.routes[1].predicates[0]=Path=/GENERATOR-SERVICE/api/generator/**
  ```
- `lb://dataset-manager` = "load balanced", l'adresse réelle est résolue via Eureka par le nom du service.
- `spring.cloud.gateway.discovery.locator.enabled=true` → en plus, des routes automatiques sont créées pour **chaque** service Eureka (ex: `/dataset-manager/**`, `/generator/**`).

⚠️ **À tester ce soir avant de te coucher** : vérifie que les deux familles de routes répondent bien (voir checklist en bas). Si la route `/DATASET-MANAGER/...` ne fonctionne pas comme prévu, utilise `/dataset-manager/...` (minuscule, auto-découverte) pendant la démo — explique simplement que c'est la route via la découverte Eureka.

### d) OpenFeign — `generator_service`

- `generator_service/src/main/java/.../feign/DatasetManagerClient.java` :
  ```java
  @FeignClient(name = "dataset-manager", fallback = DatasetManagerClientFallback.class)
  public interface DatasetManagerClient {
      @GetMapping("/api/projects/{projectId}")
      ProjectResponseDTO getProject(@PathVariable Long projectId);

      @GetMapping("/api/projects/{projectId}/entities")
      List<DataEntityResponseDTO> getProjectEntities(@PathVariable Long projectId);
  }
  ```
- `name = "dataset-manager"` = le nom Eureka du service cible. Pas d'URL en dur !
- `@EnableFeignClients` dans `GeneratorServiceApplication.java` active le scan de ces interfaces.
- Utilisé dans `DataGenerationServiceImpl` (`generator_service/src/main/java/.../service/DataGenerationServiceImpl.java`) pour récupérer la définition du projet avant de générer les données.

### e) Resilience4J — `generator_service`

Config dans `config-repo/generator.properties` :
```properties
spring.cloud.openfeign.circuitbreaker.enabled=true

resilience4j.circuitbreaker.instances.datasetManagerClient.slidingWindowSize=10
resilience4j.circuitbreaker.instances.datasetManagerClient.minimumNumberOfCalls=5
resilience4j.circuitbreaker.instances.datasetManagerClient.failureRateThreshold=50
resilience4j.circuitbreaker.instances.datasetManagerClient.waitDurationInOpenState=5s
resilience4j.circuitbreaker.instances.datasetManagerClient.permittedNumberOfCallsInHalfOpenState=3
resilience4j.circuitbreaker.instances.datasetManagerClient.automaticTransitionFromOpenToHalfOpenEnabled=true

resilience4j.timelimiter.instances.datasetManagerClient.timeoutDuration=10s
resilience4j.timelimiter.instances.datasetManagerClient.cancelRunningFuture=true
```

**Comment l'expliquer simplement** :
- Sur les 10 derniers appels (`slidingWindowSize`), si on a fait au moins 5 appels (`minimumNumberOfCalls`) et que 50% ou plus ont échoué (`failureRateThreshold`), le circuit passe **OPEN**.
- En `OPEN`, pendant 5 secondes (`waitDurationInOpenState`), on n'essaie même plus d'appeler `dataset-manager` — fallback immédiat.
- Après ces 5 secondes, le circuit passe en `HALF_OPEN` et autorise 3 appels test (`permittedNumberOfCallsInHalfOpenState`). S'ils passent, retour à `CLOSED`.
- Le `TimeLimiter` : si un appel dépasse 10s, il est annulé et compté comme un échec (indépendamment du circuit breaker).

Le fallback : `generator_service/src/main/java/.../feign/DatasetManagerClientFallback.java`
```java
public ProjectResponseDTO getProject(Long projectId) {
    return null; // déclenche le mode "PARTIAL" côté DataGenerationServiceImpl
}
public List<DataEntityResponseDTO> getProjectEntities(Long projectId) {
    return List.of();
}
```

Et l'utilisation du fallback dans `DataGenerationServiceImpl.generatePreview()` :
```java
if (project == null) {
    return PreviewResponseDTO.builder()
        .status("PARTIAL")
        .message("Le service dataset-manager est indisponible. Veuillez réessayer plus tard.")
        .data(List.of())
        .build();
}
```
→ **200 OK**, jamais de stacktrace, même si `dataset-manager` est mort. C'est exactement ce que le prof va tester.

---

## 5. Scénario de démo (script à suivre)

1. **Montrer Eureka** (http://localhost:8761) : tous les services sont enregistrés.
2. **Créer un projet** (Swagger sur `dataset-manager` : http://localhost:8081/api/swagger-ui/index.html, ou via la gateway) :
   ```json
   POST /api/projects
   { "name": "Demo Soutenance" }
   ```
3. **Ajouter une entité** :
   ```json
   POST /api/projects/1/entities
   {
     "name": "Utilisateur",
     "quantity": 5,
     "attributes": [
       { "name": "fullName", "type": { "typeClass": "StringType" } },
       { "name": "email", "type": { "typeClass": "StringType" } },
       { "name": "age", "type": { "typeClass": "IntegerType" }, "constraints": [{ "constraintClass": "MinMaxConstraint", "min": 18, "max": 65 }] }
     ]
   }
   ```
4. **Demander un aperçu** (montre que generator récupère bien la définition via Feign et génère les données) :
   ```
   GET http://localhost:8082/api/generator/1/preview
   ```
   → On voit 5 lignes avec un nom, un email et un âge entre 18 et 65, générés par Datafaker.
5. **Exporter** (montrer 1 ou 2 formats) :
   ```
   GET http://localhost:8082/api/generator/1/export?format=json
   GET http://localhost:8082/api/generator/1/export?format=csv
   ```
6. **LE moment clé : couper `dataset_manager_service`** (Ctrl+C dans son terminal).
7. **Refaire le `/preview`** → toujours 200 OK, mais `status: "PARTIAL"`, `data: []`, message explicite. **Pas de stacktrace.** C'est le scénario de validation demandé par le prof.
8. (Optionnel, si le temps) Relancer `dataset_manager_service`, attendre ~5-10s, refaire `/preview` → ça remarche (le circuit revient en `CLOSED`).

---

## 6. Côté `dataset_manager_service` (CRUD) — points à connaître

- **Couche classique** : `controller/` → `service/` (interface + impl) → `repository/` (Spring Data JPA) → `model/entities/` (JPA).
- **Modèle polymorphe** : `Attribute` a un `type` (`BaseDataType` : Integer/Float/String/Boolean/Date/Enum) et une liste de `constraints` (`BaseConstraint` : MinMax, Enum). Stockés via `@ManyToOne`/`@ManyToMany` avec héritage JPA (table par défaut `SINGLE_TABLE` ou jointure selon l'annotation sur `BaseDataType`/`BaseConstraint` — à vérifier si on te le demande).
- **Mapping DTO ↔ Entité** :
  - `ProjectMapper` → **MapStruct** (`@Mapper(componentModel = "spring")`), génération auto au build.
  - `DataEntityMapper` → mapping **manuel**, parce que le type/contrainte est polymorphe : on utilise des `Map<Class<...>, Function<...>>` comme "registre" pour convertir chaque sous-type. C'est volontairement fait à la main pour respecter l'OCP (ajouter un type = ajouter une entrée dans la map, sans toucher au reste).
- **Gestion des erreurs** : `GlobalExceptionHandler` (`@RestControllerAdvice`) intercepte tout :
  - `EntityNotFoundException` → 404
  - `MethodArgumentNotValidException` (`@Valid` échoué) → 400 avec le détail des champs en erreur
  - `DataIntegrityViolationException` (doublon, contrainte unique) → 409
  - `Exception` générique → 500 sans détail technique exposé au client (mais loggé côté serveur)
- **CORS** : `config/WebConfig.java`, origines autorisées configurables (`http://localhost:5173/5174`, ports Vite par défaut — pour un éventuel frontend).

---

## 7. Côté `generator_service` (génération) — points à connaître

- **`GeneratorController`** : 2 endpoints, `/preview` (retourne du JSON) et `/export` (retourne un fichier téléchargeable selon `format`).
- **`DataGenerationServiceImpl`** : orchestre tout — appelle Feign pour avoir la structure, puis `RecordGenerator` + `FakerDataGenerator` pour produire les valeurs, gère aussi les **sous-entités** récursivement (relations one-to-many, ex: un Client → plusieurs Commandes).
- **`FakerDataGenerator`** : pour chaque attribut, on essaie d'abord de **deviner le domaine via le nom** (stratégies dans `generation/strategy/` : Identity, Contact, Address, Company, Internet/Text, Commerce). Si rien ne matche, on génère selon le **type** (`IntegerType` → nombre dans les bornes `MinMaxConstraint`, `EnumType` → valeur aléatoire parmi `values`, etc.). Pattern **Stratégie** + **OCP** : ajouter un domaine = ajouter une classe dans `strategy/`.
- **Export** : `ExporterFactory` + 4 implémentations de `IDataExporter` (JSON, XML, CSV, **SQL**). Le SQL est un bonus (l'énoncé demandait JSON/XML/CSV).
- **Stateless** : aucune dépendance JPA/H2 dans le `pom.xml` de ce module — à montrer si demandé, c'est un argument fort pour "on peut scaler horizontalement".

---

## 8. "Ce qu'on a fait en plus" (à mentionner dans les 10 min)

- **4 formats d'export** au lieu de 3 (JSON, XML, CSV, **+ SQL**).
- **Swagger / OpenAPI** sur `dataset-manager` (springdoc).
- **Console H2** activée pour inspecter la base pendant le dev.
- **Gestion d'erreurs centralisée** avec codes HTTP corrects (400/404/409/500) et messages clairs, pas de stacktrace exposée.
- **Génération récursive de sous-entités** (relations one-to-many imbriquées).
- **Détection automatique du type de donnée par nom d'attribut** (7 stratégies Faker) — pas juste un générateur générique.
- README principal mis à jour avec toute l'archi + scénario de validation.

---

## 9. Questions probables du prof + comment répondre

**"Pourquoi avoir séparé le generator du manager ?"**
→ Isoler la charge CPU de la génération (qui peut être lourde sur gros volumes) du reste de l'API, et pouvoir scaler le generator indépendamment (plusieurs instances stateless) sans toucher à la base de données.

**"Pourquoi Eureka plutôt que des URLs en dur (`localhost:8081`) ?"**
→ Permet le scaling horizontal (plusieurs instances du même service avec load balancing automatique) et évite de recompiler/redéployer si un port ou une IP change.

**"Pourquoi un Config Server ?"**
→ Toute la config (ports, routes gateway, paramètres Resilience4J, URL Eureka) est dans un seul endroit (`config-repo/`), versionnée, modifiable sans toucher au code des services.

**"Que se passe-t-il si `generator-service` lui-même tombe (pas `dataset-manager`) ?"**
→ Honnête : pas de fallback spécifique pour ce cas (ce n'était pas demandé) — la gateway renverrait une erreur 503 si plus aucune instance n'est dispo dans Eureka. On pourrait ajouter un Circuit Breaker côté Gateway si on voulait aller plus loin.

**"TimeLimiter vs Circuit Breaker, c'est pas redondant ?"**
→ Non : le TimeLimiter agit **par appel** (annule un appel trop long). Le Circuit Breaker agit **sur l'historique** (trop d'échecs récents → on arrête d'essayer pendant un moment). Les deux se combinent : un appel trop long compte comme un échec pour le Circuit Breaker.

**"Le `generator-service` est-il vraiment stateless ?"**
→ Oui : pas de base de données, pas de variable d'instance modifiée entre 2 requêtes (`FakerDataGenerator` est instancié à la demande dans `DataGenerationServiceImpl`). Donc 2 instances peuvent tourner en parallèle sans se marcher dessus.

**"Et si je vous demande d'ajouter un nouveau type d'attribut, ex: `LongType` ?"** → voir section 10.

---

## 10. Si le prof demande de modifier le code en live

Quelques scénarios fréquents et où aller **directement** (pour ne pas chercher pendant 5 minutes) :

| Demande | Fichier(s) à ouvrir |
|---|---|
| Changer le seuil/délai du Circuit Breaker | `config-repo/generator.properties` (section `resilience4j.circuitbreaker...`) |
| Changer le message du fallback | `generator_service/.../service/DataGenerationServiceImpl.java` (méthode `generatePreview`) |
| Ajouter un nouveau format d'export (ex: YAML) | `generator_service/.../export/` : créer `YAMLExporter implements IDataExporter`, l'enregistrer dans `ExporterFactory` (3 maps : exporters, mediaTypes, extensions) |
| Ajouter une nouvelle route Gateway | `config-repo/gateway.properties` (ajouter `routes[2]...`) |
| Ajouter un nouveau type de donnée (ex: `LongType`) | 1) `model/types/LongType.java` (extends `BaseDataType`) + `dto/model/types/LongTypeDTO.java` 2) enregistrer dans `DataEntityMapper.TYPE_TO_DTO` / `DTO_TO_TYPE` 3) côté generator, ajouter le `case "LongType"` dans `FakerDataGenerator.generateByTypeFromDTO` |
| Ajouter un endpoint CRUD simple (ex: GET stats d'un projet) | Ajouter méthode dans `ProjectController` → `ProjectService`/`Impl` → éventuellement `ProjectRepository` (requête dérivée Spring Data) |
| Changer la quantité max générée par défaut | `dto/entity/DataEntityCreateDTO.java` (validation `@Min`) côté manager |
| Tester le fallback en live | Couper `dataset_manager_service`, refaire `/api/generator/{id}/preview` |

**Conseil** : avant la soutenance, ouvre ces fichiers une fois dans ton IDE pour qu'ils soient dans l'historique récent — ça va beaucoup plus vite à retrouver avec Ctrl+E / Ctrl+Shift+N (IntelliJ) sous le stress.

---

## 11. Checklist à faire CE SOIR (avant de dormir tranquille)

- [ ] Lancer les 5 services dans l'ordre (config → discovery → manager → generator → gateway), vérifier qu'ils démarrent **sans erreur** et apparaissent tous dans Eureka (http://localhost:8761).
- [ ] Tester le scénario complet de la section 5 de bout en bout (créer projet/entité, preview, export, couper le manager, re-preview = PARTIAL).
- [ ] Tester les routes Gateway : `http://localhost:8083/dataset-manager/api/projects` et `http://localhost:8083/generator/api/generator/1/preview`. Si `/DATASET-MANAGER/...` (majuscules) ne marche pas, le savoir et avoir la version minuscule en backup.
- [ ] Préparer une **collection Postman/Bruno** ou des requêtes prêtes (pas à retaper en live).
- [ ] Relire ce fichier une fois à voix haute pour le pitch de 30 secondes (section 1).
- [ ] Avoir l'IDE ouvert avec les fichiers clés (section 10) déjà visités.

---

## 12. Si le prof creuse sur les points moins finis (réponse honnête)

- **Le champ `generator` (FakerGenerator/SequentialGenerator, locale, valeur de départ) défini sur un attribut n'est pas encore propagé jusqu'au `generator-service`** : la génération se base aujourd'hui sur le nom + le type de l'attribut. Si on te pose la question : *"on a priorisé le découpage microservices + la résilience demandés par l'énoncé ; la sélection explicite du générateur par attribut est identifiée comme prochaine étape."*
- **Pas de Docker** : optionnel dans l'énoncé, pas fait par manque de temps.
- Reste honnête sur ces points si on te pose la question directement — c'est beaucoup mieux vu que de bluffer, et ça montre que tu maîtrises ton code.

Bon courage pour demain.