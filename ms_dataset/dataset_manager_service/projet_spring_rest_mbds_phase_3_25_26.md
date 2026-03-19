# Phase 3 : Tests d'API & Validation Automatisée

## Objectif

Maintenant que votre API est fonctionnelle (Phase 2), il est impératif de garantir sa fiabilité et sa robustesse. L'objectif de cette phase est de construire une **suite de tests automatisés** capable de valider le bon fonctionnement du backend et de détecter les régressions.

Vous apprendrez à structurer une collection de tests, à manipuler des variables d'environnement pour créer des scénarios dynamiques, et à générer des rapports d'exécution via un outil en ligne de commande (CLI).

## Outils autorisés

Vous avez le choix entre deux écosystèmes :

1.  **Postman** (GUI) + **Newman** (CLI)
    
2.  **Bruno** (GUI) + **Bruno CLI** (CLI)
    

## 1. Structure de la Collection de Tests

Vous devez fournir une collection unique (export JSON) organisée rigoureusement en **dossiers**. Chaque requête doit contenir **au moins un test (assertion)** (ex: vérification du code HTTP 200, vérification qu'un champ est présent dans le JSON retourné).

### Dossier A : Tests Unitaires (Cas Passants)

Ce dossier doit contenir des requêtes isolées testant individuellement chaque endpoint avec des données valides.

-   Exemple : `GET /projects`, `GET /projects/{id}` (avec un ID codé en dur ou existant), `POST /projects` (cas simple).
    
-   **Objectif :** Vérifier que chaque brique fonctionne indépendamment.
    

### Dossier B : Gestion des Erreurs (Edge Cases)

Ce dossier doit prouver que votre API gère correctement les mauvaises requêtes. Vous devez implémenter un test pour chaque type d'erreur possible.

-   **404 Not Found :** Essayer de récupérer un projet avec un ID inexistant (ex: `999999`).
    
-   **400 Bad Request :**
    
    -   Créer un projet sans nom.
        
    -   Créer une entité avec un `min` supérieur au `max`.
        
    -   Envoyer un type de champ inconnu.
        
-   **Objectif :** Vérifier que l'API ne crashe pas (500) mais renvoie une erreur client explicite.
    

### Dossier C : Scénario Nominal (Integration Test)

C'est la partie la plus critique. Vous devez scripter une histoire complète ("Happy Path") qui s'exécute de bout en bout sans intervention manuelle. Pour cela, vous utiliserez des **variables d'environnement** pour chaîner les requêtes.

**Déroulé type du scénario :**

1.  **Créer un projet** (`POST`) $\rightarrow$ _Script : récupérer l'ID du projet créé et le stocker dans la variable `{{current_project_id}}`._
    
2.  **Ajouter une entité** (`POST` sur `/projects/{{current_project_id}}/entities`) $\rightarrow$ _Script : stocker l'ID de l'entité dans `{{current_entity_id}}`._
    
3.  **Modifier l'entité** (`PUT` sur `/entities/{{current_entity_id}}`).
    
4.  **Prévisualiser les données** (`GET` sur `/projects/{{current_project_id}}/preview`).
    
5.  **Export JSON** (`GET` export).
    
6.  **Nettoyage** (`DELETE` sur `/projects/{{current_project_id}}`).
    

> **Note :** Si votre scénario nominal est exhaustif et couvre tous les endpoints "Passants", le _Dossier A_ devient optionnel. Cependant, conserver les deux est une bonne pratique (Unit vs Integration).
> 
> **Contrainte :** Ce scénario doit être **rejouable à l'infini** (Idempotence). Grâce à l'étape de nettoyage (DELETE) ou à l'utilisation de données aléatoires, on ne doit jamais avoir d'erreur "Duplicate Entry" si on lance le test deux fois de suite.

## 2. Automatisation et Rapport (CLI)

Une fois la collection validée dans l'interface graphique, vous devez simuler une exécution dans un pipeline CI/CD.

1.  Exportez votre **Collection** et votre fichier d'**Environnement** (contenant les variables initiales comme `base_url`).
    
2.  Utilisez l'outil CLI correspondant (**Newman** pour Postman ou **Bru CLI** pour Bruno).
    
3.  Générez un **rapport d'exécution** (HTML ou JSON/Text).
    

## 3. Livrables attendus

Tout doit être inclus dans votre dépôt Git, dans un dossier nommé `tests/`. **Aucun lien de partage (Postman public workspace, etc.) ne sera accepté.**

1.  **Fichier de Collection (`.json` ou format Bruno)** : Contenant les 3 dossiers et les scripts de tests/variables.
    
2.  **Fichier d'Environnement (`.json`)** : Contenant la configuration de base (ex: `host: localhost:8080`).
    
3.  **Rapport d'exécution** : Un exemple de rapport généré (fichier `.html` ou `.txt`) prouvant que les tests passent.
    
4.  **Mise à jour du README** :
    
    -   Section "Tests & QA".
        
    -   Instructions précises pour installer l'outil CLI (npm install newman...).
        
    -   La ligne de commande exacte à copier-coller pour lancer les tests et générer le rapport.
        

## 4. Critères d'évaluation

-   **Couverture :** Tous les endpoints sont testés (succès et échecs).
    
-   **Chaînage :** Le scénario nominal utilise correctement les variables pour passer les IDs d'une requête à l'autre.
    
-   **Robustesse :** Le scénario peut être lancé 10 fois de suite sans échouer.
    
-   **Assertions :** Les tests vérifient réellement le contenu (pas juste le code 200).
    
-   **Documentation :** Le correcteur peut lancer les tests sans deviner la commande.
