# 📋 Guide des Tâches - Architecture Microservices

## 🎯 Objectif du Projet

Refondre l'architecture pour isoler la logique de génération dans un service dédié (`generator-service`) et permettre la communication inter-services via **OpenFeign** avec gestion des pannes via **Resilience4J**.

---

## 👥 Répartition des Tâches (2 Personnes)

### ⏱️ Temps Estimé
- **Développement parallèle** : ~1h20 (simultané)
- **Tests finaux ensemble** : 30 min
- **Total** : ~1h50

---

## 👤 PERSONNE 1 : Generator Service (Création)

**Objectif** : Construire le `generator-service` de A à Z avec communication Feign et Circuit Breaker

### 📂 Étape 1 : Créer la structure de packages (5 min)

Créer l'arborescence suivante dans `generator_service/src/main/java/com/example/generator_service/` :

```
generator_service/src/main/java/com/example/generator_service/
├── controller/
├── service/
├── feign/
├── dto/
│   ├── entity/
│   ├── generation/
│   ├── model/
│   │   ├── types/
│   │   ├── constraints/
│   │   └── generators/
│   └── project/
├── export/
│   └── interfaces/
├── generation/
│   ├── interfaces/
│   └── strategy/
├── dataset/
├── distribution/
├── source/
└── util/
```

**Commande rapide** :
```bash
cd generator_service/src/main/java/com/example/generator_service/
mkdir -p controller service feign dto/entity dto/generation dto/model/types dto/model/constraints dto/model/generators dto/project export/interfaces generation/interfaces generation/strategy dataset distribution source util
```

---

### 📦 Étape 2 : Copier les DTOs (10 min)

Copier depuis `dataset_manager_service/src/main/java/com/example/microservice/dto/` vers `generator_service/src/main/java/com/example/generator_service/dto/`

**Fichiers à copier** :

#### `dto/entity/`
- `DataEntityResponseDTO.java`
- `AttributeDTO.java`
- `DataEntityCreateDTO.java`
- `DataEntityUpdateDTO.java`

#### `dto/generation/`
- `PreviewResponseDTO.java`
- `EntityDataDTO.java`
- `RecordDTO.java`

#### `dto/model/types/`
- `DataTypeDTO.java`
- `StringTypeDTO.java`
- `IntegerTypeDTO.java`
- `BooleanTypeDTO.java`
- `FloatTypeDTO.java`
- `DateTypeDTO.java`
- `EnumTypeDTO.java`

#### `dto/model/constraints/`
- `ConstraintDTO.java`
- `MinMaxConstraintDTO.java`
- `EnumConstraintDTO.java`

#### `dto/model/generators/`
- `GeneratorDTO.java`
- `FakerGeneratorDTO.java`
- `SequentialGeneratorDTO.java`

#### `dto/project/`
- `ProjectResponseDTO.java`

**⚠️ Important** : Changer les `package` dans chaque fichier copié :
```java
// Avant
package com.example.microservice.dto.entity;

// Après
package com.example.generator_service.dto.entity;
```

---

### 🔧 Étape 3 : Copier les classes de génération (15 min)

Copier depuis `dataset_manager_service/src/main/java/com/example/microservice/` vers `generator_service/src/main/java/com/example/generator_service/`

**Dossiers et fichiers à copier** :

#### `generation/` (tout le dossier)
- `BaseDataGenerator.java`
- `FakerDataGenerator.java`
- `SequentialDataGenerator.java`
- `interfaces/IDataGenerator.java`
- `strategy/` (tous les fichiers)

#### `dataset/`
- `Dataset.java`
- `Record.java`

#### `export/` (tout le dossier)
- `ExporterFactory.java`
- `JSONExporter.java`
- `XMLExporter.java`
- `SQLExporter.java`
- `CSVExporter.java`
- `interfaces/IDataExporter.java`

#### `distribution/` (tout le dossier)
- Tous les fichiers

#### `source/` (tout le dossier)
- Tous les fichiers

#### `util/`
- `RecordGenerator.java`

**⚠️ Important** : Changer les `package` dans TOUS les fichiers copiés

---

### 🌐 Étape 4 : Créer le FeignClient (10 min)

**Fichier** : `generator_service/src/main/java/com/example/generator_service/feign/DatasetManagerClient.java`

```java
package com.example.generator_service.feign;

import com.example.generator_service.dto.entity.DataEntityResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * FeignClient pour communiquer avec le Dataset Manager Service.
 * Récupère la structure des projets (entités, attributs, contraintes).
 */
@FeignClient(name = "dataset-manager")
public interface DatasetManagerClient {

    /**
     * Récupère toutes les entités d'un projet avec leurs attributs.
     * @param projectId ID du projet
     * @return Liste des entités avec leur structure complète
     */
    @GetMapping("/api/projects/{projectId}/entities")
    List<DataEntityResponseDTO> getProjectEntities(@PathVariable("projectId") Long projectId);
}
```

---

### 🛠️ Étape 5 : Créer le Service de génération (15 min)

**Fichier 1** : `generator_service/src/main/java/com/example/generator_service/service/DataGenerationService.java`

```java
package com.example.generator_service.service;

import com.example.generator_service.dataset.Dataset;
import com.example.generator_service.dto.generation.PreviewResponseDTO;

public interface DataGenerationService {

    /**
     * Génère un aperçu des données (10 lignes)
     */
    PreviewResponseDTO generatePreview(Long projectId);

    /**
     * Génère le dataset complet
     */
    Dataset generateDataset(Long projectId);
}
```

**Fichier 2** : `generator_service/src/main/java/com/example/generator_service/service/DataGenerationServiceImpl.java`

**Action** : Copier le contenu de `dataset_manager_service/service/DataGenerationServiceImpl.java` et modifier :

1. Changer le package
2. Injecter `DatasetManagerClient` au lieu de `DataEntityService`
3. Remplacer l'appel au repository par l'appel Feign :

```java
// Avant (dans dataset-manager)
List<DataEntity> entities = dataEntityService.findEntitiesByProject(projectId);

// Après (dans generator)
List<DataEntityResponseDTO> entities = datasetManagerClient.getProjectEntities(projectId);
```

4. Adapter la logique pour utiliser les DTOs au lieu des entités JPA

---

### 🎮 Étape 6 : Créer le Controller (10 min)

**Fichier** : `generator_service/src/main/java/com/example/generator_service/controller/GeneratorController.java`

```java
package com.example.generator_service.controller;

import com.example.generator_service.dataset.Dataset;
import com.example.generator_service.dto.generation.PreviewResponseDTO;
import com.example.generator_service.export.ExporterFactory;
import com.example.generator_service.export.interfaces.IDataExporter;
import com.example.generator_service.service.DataGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Controller pour la génération et l'export de données.
 * Endpoints appelés via la Gateway.
 */
@RestController
@RequestMapping("/api/generator")
@RequiredArgsConstructor
public class GeneratorController {

    private final DataGenerationService dataGenerationService;
    private final ExporterFactory exporterFactory;

    /**
     * Génère un aperçu des données pour un projet.
     * GET /api/generator/{projectId}/preview
     */
    @GetMapping("/{projectId}/preview")
    public PreviewResponseDTO generatePreview(@PathVariable("projectId") Long projectId) {
        return dataGenerationService.generatePreview(projectId);
    }

    /**
     * Exporte les données générées dans un format spécifique.
     * GET /api/generator/{projectId}/export?format=json|xml|sql|csv
     */
    @GetMapping("/{projectId}/export")
    public ResponseEntity<Resource> exportData(
            @PathVariable("projectId") Long projectId,
            @RequestParam(value = "format", defaultValue = "json") String format) throws IOException {

        // Générer le dataset
        Dataset dataset = dataGenerationService.generateDataset(projectId);

        // Créer un répertoire temporaire pour l'export
        Path tempDir = Files.createTempDirectory("dataset-export-");
        IDataExporter exporter = exporterFactory.getExporter(format);
        MediaType mediaType = exporterFactory.getMediaType(format);
        String fileName = "dataset_" + projectId + exporterFactory.getExtension(format);
        String filePath = tempDir.resolve(fileName).toString();

        // Exporter les données
        exporter.export(dataset, filePath);

        // Préparer la réponse avec le fichier
        File file = new File(filePath);
        Resource resource = new FileSystemResource(file);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(mediaType)
                .body(resource);
    }
}
```

---

### 🛡️ Étape 7 : Implémenter Resilience4j + Fallback (15 min)

#### 7.1 Créer le Fallback

**Fichier** : `generator_service/src/main/java/com/example/generator_service/feign/DatasetManagerClientFallback.java`

```java
package com.example.generator_service.feign;

import com.example.generator_service.dto.entity.DataEntityResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Fallback pour le DatasetManagerClient.
 * Retourne une réponse par défaut en cas de panne du dataset-manager.
 */
@Slf4j
@Component
public class DatasetManagerClientFallback implements DatasetManagerClient {

    @Override
    public List<DataEntityResponseDTO> getProjectEntities(Long projectId) {
        log.warn("Fallback activé pour le projet {}. Dataset Manager Service indisponible.", projectId);

        // Retourner une liste vide au lieu de crasher
        return Collections.emptyList();
    }
}
```

#### 7.2 Modifier le FeignClient

**Fichier** : `generator_service/feign/DatasetManagerClient.java`

```java
@FeignClient(
    name = "dataset-manager",
    fallback = DatasetManagerClientFallback.class  // Ajouter cette ligne
)
public interface DatasetManagerClient {
    // ... reste du code
}
```

#### 7.3 Ajouter l'annotation Circuit Breaker dans le Service

**Fichier** : `generator_service/service/DataGenerationServiceImpl.java`

Ajouter au-dessus de la méthode qui appelle Feign :

```java
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
@RequiredArgsConstructor
public class DataGenerationServiceImpl implements DataGenerationService {

    private final DatasetManagerClient datasetManagerClient;

    @Override
    @CircuitBreaker(name = "datasetManagerClient", fallbackMethod = "generatePreviewFallback")
    public PreviewResponseDTO generatePreview(Long projectId) {
        List<DataEntityResponseDTO> entities = datasetManagerClient.getProjectEntities(projectId);

        // Si le fallback retourne une liste vide, on gère
        if (entities.isEmpty()) {
            return PreviewResponseDTO.builder()
                .status("PARTIAL")
                .message("Service de génération momentanément indisponible")
                .data(Collections.emptyList())
                .build();
        }

        // ... reste de la logique de génération
    }

    // Méthode fallback
    public PreviewResponseDTO generatePreviewFallback(Long projectId, Exception ex) {
        return PreviewResponseDTO.builder()
            .status("ERROR")
            .message("Service de génération momentanément indisponible. Veuillez réessayer plus tard.")
            .data(Collections.emptyList())
            .build();
    }
}
```

---

### 📦 Étape 8 : Ajouter la dépendance DataFaker (2 min)

**Fichier** : `generator_service/pom.xml`

Ajouter dans la section `<dependencies>` :

```xml
<dependency>
    <groupId>net.datafaker</groupId>
    <artifactId>datafaker</artifactId>
    <version>2.4.2</version>
</dependency>
```

---

### ✅ Étape 9 : Tests isolés (10 min)

**Pré-requis** :
- Config Server running (8888)
- Discovery Server running (8761)
- Dataset Manager running (8081) - version originale

**Tests** :

```bash
# 1. Démarrer le generator-service
cd generator_service
mvn spring-boot:run

# 2. Vérifier l'enregistrement dans Eureka
curl http://localhost:8761/eureka/apps | grep GENERATOR

# 3. Test du preview
curl http://localhost:8082/api/generator/1/preview

# 4. Test de l'export
curl http://localhost:8082/api/generator/1/export?format=json -o test.json

# 5. Test du Circuit Breaker
# Arrêter dataset-manager
curl http://localhost:8082/api/generator/1/preview
# Doit retourner : {"status": "ERROR", "message": "Service momentanément indisponible"}

# 6. Redémarrer dataset-manager et re-tester
curl http://localhost:8082/api/generator/1/preview
# Doit fonctionner normalement
```

---

## 👥 PERSONNE 2 : Dataset Manager (Nettoyage)

**Objectif** : Supprimer la logique de génération du `dataset-manager-service`

### 🗑️ Étape 1 : Supprimer ProjectGenerationController (2 min)

**Fichier à supprimer** :
```bash
rm dataset_manager_service/src/main/java/com/example/microservice/controller/ProjectGenerationController.java
```

---

### 🗑️ Étape 2 : Supprimer DataGenerationService (5 min)

**Fichiers à supprimer** :
```bash
rm dataset_manager_service/src/main/java/com/example/microservice/service/DataGenerationService.java
rm dataset_manager_service/src/main/java/com/example/microservice/service/DataGenerationServiceImpl.java
```

---

### 🗑️ Étape 3 : Supprimer les Exporters (5 min)

**Dossier à supprimer** :
```bash
rm -rf dataset_manager_service/src/main/java/com/example/microservice/export/
```

---

### 🗑️ Étape 4 : Supprimer les classes de génération (5 min)

**Dossiers à supprimer** :
```bash
rm -rf dataset_manager_service/src/main/java/com/example/microservice/generation/
rm -rf dataset_manager_service/src/main/java/com/example/microservice/dataset/
rm -rf dataset_manager_service/src/main/java/com/example/microservice/distribution/
rm -rf dataset_manager_service/src/main/java/com/example/microservice/source/
rm dataset_manager_service/src/main/java/com/example/microservice/util/RecordGenerator.java
```

---

### 🗑️ Étape 5 : (Optionnel) Nettoyer les DTOs de génération (5 min)

**Vérifier** si `dto/generation/` est utilisé ailleurs dans le projet.

Si **non utilisé** :
```bash
rm -rf dataset_manager_service/src/main/java/com/example/microservice/dto/generation/
```

Si **utilisé** : laisser tel quel.

---

### 🌐 Étape 6 : (Optionnel) Créer un FeignClient vers Generator (20 min)

**Objectif** : Permettre au dataset-manager de déclencher une génération (proxy vers generator)

#### 6.1 Créer le FeignClient

**Fichier** : `dataset_manager_service/src/main/java/com/example/microservice/feign/GeneratorClient.java`

```java
package com.example.microservice.feign;

import com.example.microservice.dto.generation.PreviewResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * FeignClient pour communiquer avec le Generator Service.
 * Permet de déléguer la génération de données.
 */
@FeignClient(name = "generator")
public interface GeneratorClient {

    @GetMapping("/api/generator/{projectId}/preview")
    PreviewResponseDTO generatePreview(@PathVariable("projectId") Long projectId);

    @GetMapping("/api/generator/{projectId}/export")
    ResponseEntity<Resource> exportData(
        @PathVariable("projectId") Long projectId,
        @RequestParam("format") String format);
}
```

#### 6.2 Créer le Controller Proxy

**Fichier** : `dataset_manager_service/src/main/java/com/example/microservice/controller/ProjectGenerationProxyController.java`

```java
package com.example.microservice.controller;

import com.example.microservice.dto.generation.PreviewResponseDTO;
import com.example.microservice.feign.GeneratorClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller proxy qui délègue la génération au Generator Service.
 * Maintient la rétrocompatibilité des anciennes URLs.
 */
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectGenerationProxyController {

    private final GeneratorClient generatorClient;

    /**
     * Génère un aperçu - délégué au generator-service
     * GET /api/projects/{id}/preview
     */
    @GetMapping("/{id}/preview")
    public PreviewResponseDTO generatePreview(@PathVariable("id") Long id) {
        return generatorClient.generatePreview(id);
    }

    /**
     * Exporte les données - délégué au generator-service
     * GET /api/projects/{id}/export?format=json|xml|sql|csv
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<Resource> exportData(
            @PathVariable("id") Long id,
            @RequestParam(value = "format", defaultValue = "json") String format) {
        return generatorClient.exportData(id, format);
    }
}
```

**📝 Note** : Cette étape est optionnelle. Elle permet de garder les anciennes URLs fonctionnelles si un frontend appelle encore `/api/projects/{id}/preview`.

---

### 🧪 Étape 7 : Tests isolés (10 min)

**Pré-requis** :
- Config Server running (8888)
- Discovery Server running (8761)
- Dataset Manager running (8081) - version nettoyée

**Tests** :

```bash
# 1. Vérifier que les endpoints CRUD fonctionnent toujours
curl http://localhost:8081/api/projects
curl http://localhost:8081/api/projects/1
curl http://localhost:8081/api/projects/1/entities

# 2. Créer un nouveau projet
curl -X POST http://localhost:8081/api/projects \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Project"}'

# 3. Si le proxy a été créé (étape 6)
# Démarrer generator-service (8082)
curl http://localhost:8081/api/projects/1/preview
# Doit déléguer vers generator:8082

# 4. Vérifier qu'il n'y a plus d'erreurs de compilation
cd dataset_manager_service
mvn clean compile
```

---

## 🔗 TESTS FINAUX ENSEMBLE (30 min)

Une fois les 2 parties terminées, faire les tests d'intégration complets.

### 📋 Checklist avant les tests

- [ ] Config Service démarré (8888)
- [ ] Discovery Server démarré (8761)
- [ ] Dataset Manager démarré (8081)
- [ ] Generator Service démarré (8082)
- [ ] Gateway démarré (8083)
- [ ] Tous les services enregistrés dans Eureka

**Vérifier Eureka** :
```bash
curl http://localhost:8761/eureka/apps | grep -E "<name>|<status>"
```

Doit afficher :
```xml
<name>GATEWAY</name>
<status>UP</status>
<name>GENERATOR</name>
<status>UP</status>
<name>DATASET-MANAGER</name>
<status>UP</status>
```

---

### 🧪 Test 1 : Workflow nominal (10 min)

**Via Gateway** (point d'entrée unique) :

```bash
# 1. Créer un projet
curl -X POST http://localhost:8083/DATASET-MANAGER/api/projects \
  -H "Content-Type: application/json" \
  -d '{"name": "Microservices Test Project"}'
# Réponse : {"id": 5, "name": "Microservices Test Project", ...}

# 2. Ajouter une entité
curl -X POST http://localhost:8083/DATASET-MANAGER/api/projects/5/entities \
  -H "Content-Type: application/json" \
  -d '{
    "name": "User",
    "quantity": 100,
    "attributes": [
      {
        "name": "firstName",
        "type": {"type": "string"},
        "generator": {"type": "faker", "fakerMethod": "name.firstName"}
      },
      {
        "name": "age",
        "type": {"type": "integer"},
        "generator": {"type": "faker", "fakerMethod": "number.numberBetween", "params": ["18", "80"]}
      }
    ]
  }'

# 3. Générer un aperçu VIA GENERATOR
curl http://localhost:8083/GENERATOR-SERVICE/api/generator/5/preview
# Doit retourner 10 lignes de données générées

# 4. Exporter en JSON
curl http://localhost:8083/GENERATOR-SERVICE/api/generator/5/export?format=json -o test_export.json
cat test_export.json

# 5. Exporter en XML
curl http://localhost:8083/GENERATOR-SERVICE/api/generator/5/export?format=xml -o test_export.xml

# 6. Exporter en CSV
curl http://localhost:8083/GENERATOR-SERVICE/api/generator/5/export?format=csv -o test_export.csv
```

**✅ Résultat attendu** : Tous les appels retournent 200 OK avec des données générées.

---

### 🛡️ Test 2 : Circuit Breaker (Panne du Dataset Manager) (10 min)

**Objectif** : Vérifier que le generator ne plante pas si dataset-manager est down.

```bash
# 1. Test nominal avant panne
curl http://localhost:8083/GENERATOR-SERVICE/api/generator/5/preview
# ✅ Retourne les données

# 2. ARRÊTER le dataset-manager
# (Ctrl+C dans le terminal du dataset-manager)

# 3. Re-tester la génération
curl http://localhost:8083/GENERATOR-SERVICE/api/generator/5/preview
# ✅ Doit retourner le FALLBACK :
# {
#   "status": "ERROR",
#   "message": "Service de génération momentanément indisponible. Veuillez réessayer plus tard.",
#   "data": []
# }

# 4. Vérifier qu'il n'y a PAS de stacktrace dans les logs du generator
# Regarder les logs du generator-service

# 5. Redémarrer dataset-manager
cd dataset_manager_service
mvn spring-boot:run

# 6. Attendre 5 secondes (waitDurationInOpenState configuré dans resilience4j)

# 7. Re-tester
curl http://localhost:8083/GENERATOR-SERVICE/api/generator/5/preview
# ✅ Doit refonctionner normalement (circuit fermé)
```

**✅ Résultat attendu** :
- Pas de stacktrace (erreur 500)
- Retour d'un fallback gracieux
- Auto-récupération après redémarrage

---

### 🔄 Test 3 : Communication Feign (10 min)

**Objectif** : Vérifier que generator appelle bien dataset-manager via Feign.

```bash
# 1. Activer les logs DEBUG pour Feign (optionnel)
# Ajouter dans generator_service/src/main/resources/application.properties :
# logging.level.com.example.generator_service.feign=DEBUG

# 2. Tester l'appel
curl http://localhost:8083/GENERATOR-SERVICE/api/generator/1/preview

# 3. Vérifier dans les logs du generator
# Doit afficher quelque chose comme :
# [DatasetManagerClient#getProjectEntities] ---> GET http://dataset-manager/api/projects/1/entities
# [DatasetManagerClient#getProjectEntities] <--- HTTP/1.1 200 (150ms)

# 4. Vérifier dans les logs du dataset-manager
# Doit afficher :
# GET "/api/projects/1/entities" responded with 200
```

**✅ Résultat attendu** : Logs montrent la communication Feign fonctionnelle.

---

## 📊 Critères de Validation

### ✅ Validation Technique

- [ ] Tous les services démarrent sans erreur
- [ ] Tous les services sont enregistrés dans Eureka
- [ ] Generator peut générer un aperçu via `/api/generator/{id}/preview`
- [ ] Generator peut exporter en JSON, XML, CSV, SQL
- [ ] Communication Feign fonctionne (generator → dataset-manager)
- [ ] Circuit Breaker fonctionne (pas de crash si dataset-manager est down)
- [ ] Fallback retourne une réponse valide
- [ ] Gateway route correctement les requêtes
- [ ] Pas de code de génération dans dataset-manager (nettoyage OK)

### ✅ Validation Fonctionnelle (Scénario du document)

1. [ ] Lancer tous les services → démarrent sans encombres
2. [ ] Créer un projet et demander une génération → code 200
3. [ ] Visualiser le résultat → données générées correctement
4. [ ] Couper dataset-manager → aucune stacktrace
5. [ ] Refaire une génération → fallback retourné

---

## 🚀 Ordre de Démarrage des Services

**Important** : Respecter cet ordre pour éviter les erreurs.

```bash
# 1. Config Server (infrastructure)
cd config_service
mvn spring-boot:run
# Attendre : "Started ConfigServiceApplication in X seconds"

# 2. Discovery Server (infrastructure)
cd discovery_server
mvn spring-boot:run
# Attendre : "Started EurekaServerApplication in X seconds"
# Vérifier : http://localhost:8761

# 3. Dataset Manager (service métier)
cd dataset_manager_service
mvn spring-boot:run
# Attendre enregistrement dans Eureka

# 4. Generator Service (service métier)
cd generator_service
mvn spring-boot:run
# Attendre enregistrement dans Eureka

# 5. Gateway (point d'entrée)
cd gateway
mvn spring-boot:run
# Attendre : "Started GatewayApplication in X seconds"
```

**Vérification finale** :
```bash
curl http://localhost:8761/eureka/apps
# Doit lister : GATEWAY, GENERATOR, DATASET-MANAGER
```

---

## 📝 Notes Importantes

### ⚠️ Changements de Packages

Lors de la copie de fichiers du dataset-manager vers generator, **ne pas oublier** de changer les packages :

```java
// Avant (dataset-manager)
package com.example.microservice.dto.entity;

// Après (generator)
package com.example.generator_service.dto.entity;
```

**Astuce** : Utiliser l'outil de refactoring de votre IDE (IntelliJ, Eclipse, VSCode) pour renommer automatiquement les packages.

### ⚠️ Imports

Après la copie, vérifier et corriger tous les imports :

```java
// Avant
import com.example.microservice.dto.entity.DataEntityResponseDTO;

// Après
import com.example.generator_service.dto.entity.DataEntityResponseDTO;
```

### ⚠️ Dépendances Maven

Si vous rencontrez des erreurs de compilation dans generator-service, vérifier que toutes les dépendances sont présentes dans `pom.xml` :

- `spring-cloud-starter-openfeign`
- `spring-cloud-starter-circuitbreaker-resilience4j`
- `datafaker`
- `lombok`

### ⚠️ Configuration Resilience4j

Le fichier `resilience4j-config.yml` doit rester dans `generator_service/src/main/resources/`.

---

## 🐛 Troubleshooting

### Problème : Generator ne trouve pas DatasetManagerClient

**Solution** : Vérifier que `@EnableFeignClients` est dans `GeneratorServiceApplication.java` :

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class GeneratorServiceApplication {
    // ...
}
```

### Problème : Circuit Breaker ne s'active pas

**Solution** : Vérifier que `resilience4j-config.yml` est dans `src/main/resources/` et que l'annotation utilise le bon nom :

```java
@CircuitBreaker(name = "datasetManagerClient", fallbackMethod = "generatePreviewFallback")
```

### Problème : Erreur 404 sur les routes Gateway

**Solution** : Vérifier que les services sont bien enregistrés dans Eureka :

```bash
curl http://localhost:8761/eureka/apps | grep -E "GENERATOR|DATASET-MANAGER"
```

### Problème : Feign retourne 404

**Solution** : Vérifier que l'URL dans le FeignClient correspond à l'endpoint du dataset-manager :

```java
// Generator FeignClient
@GetMapping("/api/projects/{projectId}/entities")

// Dataset Manager endpoint
@GetMapping("/projects/{projectId}/entities")  // context-path = /api
```

---

## 📚 Ressources

- **Spring Cloud OpenFeign** : https://spring.io/projects/spring-cloud-openfeign
- **Resilience4j** : https://resilience4j.readme.io/docs/circuitbreaker
- **Spring Cloud Gateway** : https://spring.io/projects/spring-cloud-gateway
- **Netflix Eureka** : https://spring.io/guides/gs/service-registration-and-discovery

---

## ✅ Checklist Finale

### Avant de push

- [ ] Tous les tests passent
- [ ] Aucune erreur de compilation
- [ ] Tous les services démarrent
- [ ] Circuit Breaker testé et fonctionnel
- [ ] Documentation README mise à jour
- [ ] Code nettoyé (pas de code commenté, pas de `System.out.println`)
- [ ] Logs pertinents ajoutés (niveau INFO ou DEBUG)

### Git

```bash
# Créer une branche (si pas déjà fait)
git checkout -b feat/add-generator-service

# Ajouter les modifications
git add .

# Commit
git commit -m "feat: add generator-service with Feign and Resilience4j

- Create generator-service with generation logic
- Implement FeignClient to dataset-manager
- Add Circuit Breaker with fallback
- Clean dataset-manager from generation code
- Update gateway routes
- Add integration tests"

# Push
git push origin feat/add-generator-service

# Créer une Pull Request ou merger directement
git checkout main
git merge feat/add-generator-service
git push origin main

# Ajouter un tag
git tag -a v2.0-microservices -m "Architecture microservices complète"
git push origin v2.0-microservices
```

---

**Bonne chance ! 🚀**
