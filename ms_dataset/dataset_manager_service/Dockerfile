# Multi-stage build pour optimiser la taille de l'image
FROM eclipse-temurin:25-jdk-alpine AS build

# Installer Maven (pas de .mvn/ wrapper dans ce module)
RUN apk add --no-cache maven

WORKDIR /app

# Copier le pom.xml et télécharger les dépendances (layer mis en cache)
COPY pom.xml ./
RUN mvn dependency:go-offline

# Copier le code source
COPY src ./src

# Builder l'application
RUN mvn clean package -DskipTests

# Image finale légère
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copier le jar depuis le stage de build
COPY --from=build /app/target/*.jar app.jar

# Créer le répertoire pour la base H2
RUN mkdir -p /app/data

# Exposer le port
EXPOSE 8081

# Volume pour persister la base H2
VOLUME /app/data

# Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
