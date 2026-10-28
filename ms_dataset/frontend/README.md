# Frontend

Interface de démonstration en **React 19 + TypeScript**, construite avec **Vite**. Elle permet de modéliser les tables d'un projet, de prévisualiser les données générées et de les exporter, sans passer par Postman ou Bruno.

## Lancer

Les services backend doivent tourner (config, Eureka, dataset-manager, generator, gateway). Puis, depuis ce dossier :

```bash
npm install
npm run dev
```

et ouvrir http://localhost:5173. Le port 5173 est imposé (`strictPort`) : c'est l'origine autorisée par le CORS de `dataset-manager` et de `generator`.

Par défaut, l'interface appelle directement `dataset-manager` (8081) et `generator` (8082). Les adresses se changent au build avec `VITE_MANAGER_URL` et `VITE_GENERATOR_URL`.

| Commande | Rôle |
|---|---|
| `npm run dev` | serveur de développement avec rechargement à chaud |
| `npm run build` | vérification des types (`tsc`), puis build de production dans `dist/` |
| `npm test` | tests Vitest de la logique (conversions formulaire ↔ API, arbre des tables) |

L'image Docker construit le front puis sert `dist/` avec `server.cjs`, un serveur statique sans dépendance.

## Structure

```
src/
  api/          types des DTO (unions discriminées comme Jackson) et appels HTTP
  lib/          logique sans React : formulaire ↔ requête, arbre des tables, libellés (testée)
  components/   ProjectSidebar, EntityTable, EntityFormPanel, GenerationPanel, Banner, Icon (SVG intégrés)
  App.tsx       état de la page et enchaînement des appels
```

## Fonctionnalités

- **Projets** : lister, créer, renommer, supprimer.
- **Tables** : créer, modifier et supprimer une table et ses colonnes (texte, entier, décimal, booléen, date, énumération ; bornes min/max). Un bouton pré-remplit une table de démonstration.
- **Sous-tables** : « + Sous-table » sur une table crée une table liée. La liste affiche l'arbre et la clé étrangère générée (`client_id → client.id`).
- **Aperçu** : jusqu’à 100 lignes par table, un onglet par table, les 20 premières lignes affichées et les colonnes de clés (`id`, `<parent>_id`) mises en évidence. Le libellé indique combien de lignes contiendra l'export. Un bandeau orange `PARTIAL` signale que `dataset-manager` ne répond plus (fallback du circuit breaker).
- **Export** : SQL (tables, clés primaires et étrangères, puis lignes), JSON, CSV et XML, envoyés en streaming par `generator`.
