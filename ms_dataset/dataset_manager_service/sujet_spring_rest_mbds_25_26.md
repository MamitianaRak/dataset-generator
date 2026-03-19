# Phase 2 : Implémentation Backend & API REST (Spring Boot)

## Objectif du projet

Après avoir modélisé le **Générateur de Datasets**, l'objectif est d'implémenter le backend et d'exposer ses fonctionnalités via une **API RESTful**.

## Stack Technique imposée

-   **Langage :** Java 25.
-   **Framework :** Spring Boot 4.0.1 (version de Spring Initialzr).
-   **Base de données :** H2 uniquement.
-   **Persistance :** Spring Data JPA.
-   **Documentation :** Springdoc OpenAPI (Swagger).
-   **Outils :** Maven ou Gradle.

##  Isolation du Contrôleur

Le **Controller** est un simple point d'entrée HTTP.
1.  Il reçoit des DTOs.
2.  Il appelle le Service.
3.  Il retourne des DTOs (ou des `ResponseEntity<DTO>`).
4.  **Interdiction formelle** d'utiliser une classe annotée `@Entity` dans les contrôleurs.
    
La conversion **DTO $\leftrightarrow$ Entity** est la responsabilité de la couche **Service** (vous pouvez coder des méthodes `toEntity`/`toDto` manuelles ou utiliser _MapStruct_, mais l'appel se fait dans le Service).

## Fonctionnalités & Endpoints

### 1. Le Projet

**Verbe HTTP** | **Endpoint** | **Action** |
|--|--|--|
| **POST** | `/api/projects` | Créer un nouveau projet. |
| **GET** | `/api/projects` |  Lister tous les projets. |
| **GET** | `/api/projects/{id}` | Obtenir le détail d'un projet. |
| **PUT** | `/api/projects/{id}` | Mettre à jour les infos d'un projet. |
| **DELETE** | `/api/projects/{id}` | Supprimer un projet. |    

### 2. Modélisation des Données 

C'est le cœur du sujet. Un utilisateur doit pouvoir envoyer un JSON décrivant une entité et ses règles de génération.

**Verbe HTTP** |  **Endpoint** | **Action** |
|--|--|--|
| **POST** | `/api/projects/{projectId}/entities` | Ajouter une entité dans un projet. |
| **GET** | `/api/projects/{projectId}/entities` |  Lister toutes les entités dans un projet |
| **GET** | `/api/entities/{id}` | Lire la configuration d'une entité. |
| **PUT** | `/api/entities/{id}` | Modifier une entité (champs & contraintes).| 
| **DELETE** |  `/api/entities/{id}` |  Supprimer une entité.|

### 3. Génération & Export
**Verbe HTTP** |  **Endpoint** | **Action** |
|--|--|--|
| **GET** |  `/api/projects/{id}/preview` | Générer un aperçu des données. |
| **GET** | `/api/projects/{id}/export` | Télécharger le fichier final (CSV/JSON/XML). |
    

## Exemple de Payload JSON

Voici un exemple de ce que votre API doit être capable de recevoir pour configurer une entité "Utilisateur" avec des contraintes spécifiques. Votre modélisation (DTO) doit pouvoir mapper une structure analogue, :warning: **concevez vous même votre propre format** :warning: 

**Scénario :** On configure une table `User` avec un ID, un nom, un âge et un rôle.

**POST** `/api/projects/1/entities`

```JSON
{
  "name": "User",
  "rowCount": 1000,
  "fields": [
    {
      "name": "id",
      "type": "AutoIncrement",
      "constraints": {
        "start": 1,
        "step": 1
      }
    },
    {
      "name": "fullName",
      "type": "Name",
      "constraints": {
        "language": "fr",
        "style": "firstname_lastname"
      }
    },
    {
      "name": "age",
      "type": "Integer",
      "constraints": {
        "min": 18,
        "max": 90,
        "distribution": "uniform"
      }
    },
    {
      "name": "email",
      "type": "Email",
      "constraints": {
        "domain": "example.com"
      }
    },
    {
      "name": "role",
      "type": "Enum",
      "constraints": {
        "values": ["ADMIN", "EDITOR", "VIEWER"],
        "probabilities": [0.1, 0.3, 0.6] 
      }
    }
  ]
}
```

> **Note  :** Remarquez le champ `options`. C'est un objet polymorphique qui dépend du `type` de champ. Votre conception Java doit gérer cette flexibilité.

## Travail attendu

1.  **Code Source :** Projet Spring et API REST dans l'assignment suivant : [https://classroom.github.com/a/2Dyfhz6E](https://classroom.github.com/a/2Dyfhz6E)

> **Note  :** Ce travail est un travail qui sera incrémenté au fur et à mesure des séances, il n'y a pas de date fixe de rendu pour l'instant et vous aurez la liste des éléments à fournir au fur et à mesure.
> Lors de la livraison finale, une soutenance sera organisée afin que vous puissiez présenter votre travail.
> La soutenance inclura une partie "Questions" auxquelles vous devrez être capable de répondre (que vous ayez fait le code ou que ce soit votre binôme, humain ou IA ;))
	 
###  Critères de notation
- Projet implémenté et fonctionnel respectant les exigences 
- La régularité de vos soumissions impactera votre notation (jusqu'à -50% si vous soumettez tout d'un coup au dernier moment). 


