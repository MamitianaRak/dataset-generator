# Frontend de demo

Interface React pour tester l'application sans passer par Postman/Bruno.
**Aucun npm install requis** : les bundles React et Babel sont deja dans `vendor/`.

## Lancer

1. S'assurer que les 5 services backend tournent (ordre habituel : config → eureka → manager → generator → gateway)
2. Depuis ce dossier :

```bash
node server.js
```

3. Ouvrir http://localhost:5173

> Port 5173 est important — c'est l'origine autorisee par le CORS configure dans `dataset_manager_service/WebConfig.java` et `@CrossOrigin` sur `GeneratorController`.

## Ce que fait le frontend

- **Sidebar** : lister / creer / supprimer des projets (appelle `dataset-manager` port 8081)
- **Entites** : ajouter / supprimer des entites avec leurs attributs (types, contraintes min/max, enum)
- **Bouton "Pre-remplir entite de demo"** : charge directement l'entite `Utilisateur` (fullName/email/age 18-65) prete a soumettre en un clic
- **Apercu (preview)** : appelle `generator-service` port 8082, affiche un tableau des donnees generees
  - Badge vert `OK` = tout va bien
  - Badge orange `PARTIAL` = dataset-manager ne repond plus (Circuit Breaker declenche) — exactement le scenario de validation de l'enonce
- **Export JSON / XML / CSV / SQL** : telecharge le fichier directement depuis `generator-service`
