# Projet : Architecture Microservices — "Dataset"

## 1. Contexte et Objectifs

Votre application "Générateur de Datasets" fonctionne bien en version monolithique. Cependant, la génération de gros volumes de données (ex: 1 million de lignes) consomme beaucoup de ressources CPU et ralentit l'interface utilisateur et la gestion des projets.

**Votre mission :** Refondre l'architecture pour isoler la logique de génération dans un service dédié et sécuriser les échanges réseaux.

**Objectifs pédagogiques :**

-   Mettre en place une **Discovery Service** (Eureka).
    
-   Centraliser les accès via une **API Gateway**.
    
-   Communiquer entre services via **OpenFeign**.
    
-   Gérer les pannes réseaux avec **Resilience4J**.

-  Mettre en place un système de **Configuration** centralisé.

- _Optionnellement_ : **Dockerization** de l'environnement complet.

## 2. Architecture Cible

Vous allez découper votre application en 5 composants distincts :

1.  **Discovery Server** (Eureka) : L'annuaire des services.
    
2.  **API Gateway** (Spring Cloud Gateway) : Le point d'entrée unique.
    
3.  **Dataset Manager Service** (L'ancien monolithe allégé) : Gère les projets, les utilisateurs et la base de données (H2/Postgres).
    
4.  **Generator Service** (Nouveau service) : Un service "ouvrier" (stateless) qui reçoit des règles de génération et renvoie les données générées.

5.  **Config Service** (Spring Cloud Config) : Le service de configuration qui sera appelé par tous les services afin de récupérer leur configuration.
    
## 3. Étapes de réalisation

### Étape 1 : Infrastructure (Eureka & Gateway & Config)

-   Créer un projet Spring Boot pour le serveur **Eureka**.
    
-   Créer un projet Spring Boot pour la **Gateway**.
    
	-   Configurer la Gateway pour router les requêtes de manière dynamique
    
	    -   `/DATASET-MANAGER/api/projects/**` -> vers `dataset-manager-service`
        
	    -   `/GENERATOR-SERVICE/api/generator/**` -> vers `generator-service`
	    
- Créer un projet Spring Boot pour le serveur de **Configuration**. 
        

### Étape 2 : Extraction du "Generator Service"

-   Créer un nouveau microservice `generator-service`.
    
-   Déplacer la logique de génération (les classes `DatasetGeneratorService`, les `Exporter`, et les logiques aléatoires) du monolithe vers ce nouveau service, ainsi que les endpoints associés (modifiez les URLs):

**Verbe HTTP** |  **Endpoint** | **Action** |
|--|--|--|
| **GET** | `/api/generator/{projectId}/preview` | Générer un aperçu des données. |
| **GET** | `/api/generator/{projectId}/export` | Télécharger le fichier final (CSV/JSON/XML). |
    
- Ces endpoints devront :
    
    -   Demander une définition de dataset au `dataset-manager-service` (JSON contenant la **structure** : entités, champs, types, contraintes ou tout autre format de votre choix).
        
    -   Renvoyer le fichier ou les données générées à l'utilisateur.
        
-   _Note : Ce service n'a pas besoin de base de données et doit être impérativement stateless afin que l'on puisse lancer plusieurs instances de ce dernier._
    

### Étape 3 : Communication via Feign

-   Dans le `dataset-manager-service` (votre ancien monolithe), supprimez la logique de génération interne.
    
-   Intégrez **OpenFeign** dans le service approprié afin que l'on puisse demander directement au `generator-service` de lancer la génération de données.
    

### Étape 4 : Résilience avec Resilience4J

Le service `dataset-manager-service` peut être lent ou indisponible. Vous devez protéger le `generator-service`.

-   Implémentez un **Circuit Breaker** sur l'appel Feign.
    
-   Définissez un **TimeLimiter** (ex: si la génération prend > x secondes, on coupe).
    
-   Mettez en place un **Fallback** :
    
    -   Si le `dataset-manager-service` est HS ou trop lent, renvoyer une réponse par défaut à l'utilisateur (ex: un JSON contenant `{"status": "PARTIAL", "data": []}` ou un message "Service de génération momentanément indisponible, veuillez réessayer plus tard").
        

## 4. Scénario de validation

1.  Lancer tous les services (Eureka, Config, Gateway, Manager, Generator) qui doivent évidemment démarrer sans encombres.
    
2.  Via Postman (ou un Front), créer un projet et demander une génération -> **Ça fonctionne (code 200).** -> visualisez le résultat brièvement.
    
3.  **Couper le service `dataset-manager-service`** (l'arrêter brutalement).
    
4.  Refaire une demande de génération.
    
5.  Le Generator ne doit pas planter (Stacktrace interdite). Il doit renvoyer le **Fallback** défini grâce à Resilience4J.
    

----------

## 5. Livrables et Contraintes
    
-   **Dockerization** (Optionnel mais recommandé) pour lancer tout l'écosystème facilement.
    
-   **Readme** : Mettez à jour votre readme avec les informations relatives à ce que vous avez ajouté pour cette étape.

### Rendu
- Le rendu se fait exclusivement sur cet assignment : https://classroom.github.com/a/2Dyfhz6E
- Vous pouvez push vos modifications directement sur la branche main (ajoutez un tag idéalement)
