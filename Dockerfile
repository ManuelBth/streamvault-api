# ==============================================================================
# Dockerfile para StreamVault API
# ==============================================================================
# Este archivo define cómo se construye la imagen de Docker de tu aplicación.
# Docker es como una máquina virtual ligera que tiene todo lo necesario para correr tu app.
# ==============================================================================

# ------------------------------------------------------------------------------
# Etapa 1: BUILD - Compilar la aplicación
# ------------------------------------------------------------------------------
# Usamos una imagen con JDK 21 para compilar
FROM eclipse-temurin:21-jdk-alpine AS build

# Directorio de trabajo
WORKDIR /app

# Copiamos el POM para descargar dependencias primero (optimización de caché)
# nota: el proyecto está en project/streamvault-api/
COPY project/streamvault-api/pom.xml project/streamvault-api/
COPY project/streamvault-api project/streamvault-api

# Nos movemos al directorio del proyecto
WORKDIR /app/project/streamvault-api

# Descargamos las dependencias
RUN apk add --no-cache maven
RUN mvn dependency:go-offline -B

# Compilamos y empaquetamos
RUN mvn package -DskipTests

# ------------------------------------------------------------------------------
# Etapa 2: RUNTIME - Imagen final más chica
# ------------------------------------------------------------------------------
# Imagen más ligera solo con JRE (no JDK) para correr la app
FROM eclipse-temurin:21-jre-alpine

# Directorio de trabajo
WORKDIR /app

# Copiamos el JAR generado en la etapa de build
COPY --from=build /app/project/streamvault-api/target/*.jar app.jar

# Puerto que expone la aplicación (Spring Boot usa 8080 por defecto)
EXPOSE 8080

# Variable de entorno para el perfil de Spring
ENV JAVA_OPTS="-Dspring.profiles.active=prod"

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ==============================================================================
# CÓMO USAR ESTE DOCKERFILE:
# ==============================================================================
#
# 1. Construir la imagen:
#    docker build -t streamvault-api .
#
# 2. Correr el contenedor:
#    docker run -p 8080:8080 streamvault-api
#
# 3. Ver los logs:
#    docker logs -f streamvault-api
#
# ==============================================================================
