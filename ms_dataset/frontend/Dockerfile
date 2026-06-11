# Image légère avec Node.js
FROM node:20-alpine

WORKDIR /app

# Copier les fichiers du frontend
COPY . .

# Installer les dépendances si nécessaire (mais normalement pas besoin, vendor/ existe déjà)
# RUN npm install

# Exposer le port
EXPOSE 5173

# Démarrer le serveur
CMD ["node", "server.js"]
