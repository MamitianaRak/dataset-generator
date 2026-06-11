# Multi-stage build pour optimiser la taille de l'image
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

# Copier les fichiers Maven
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Télécharger les dépendances (layer mis en cache si pom.xml n'a pas changé)
RUN ./mvnw dependency:go-offline

# Copier le code source
COPY src ./src

# Builder l'application
RUN ./mvnw clean package -DskipTests

# Image finale légère
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copier le jar depuis le stage de build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port
EXPOSE 8082

# Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
