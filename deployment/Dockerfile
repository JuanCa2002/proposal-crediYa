# Stage 1: build
FROM gradle:8.4-jdk17 AS builder
WORKDIR /app

# Copiar configuración de Gradle
COPY build.gradle settings.gradle gradlew gradlew.bat gradle.properties ./
COPY gradle ./gradle

# Copiar todo el proyecto (manteniendo estructura)
COPY applications /app/applications
COPY domain /app/domain
COPY infrastructure /app/infrastructure
COPY main.gradle /app/

# Compilar el jar desde el módulo app-service
RUN ./gradlew :app-service:clean :app-service:bootJar -x test -x validateStructure

# Stage 2: runtime
FROM openjdk:17-jdk-slim
WORKDIR /app

# Instalar cliente de PostgreSQL
RUN apt-get update && apt-get install -y postgresql-client && rm -rf /var/lib/apt/lists/*

# Copiar el jar construido desde el módulo correcto
COPY --from=builder /app/applications/app-service/build/libs/*.jar app.jar

# Copiar script SQL
COPY local_environment/script_database.sql /docker-entrypoint-initdb.d/script_database.sql

EXPOSE 8081

# Ejecutar script en la DB antes de levantar el micro
ENTRYPOINT ["sh", "-c", "PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -U $DB_USER -d $DB_NAME -f /docker-entrypoint-initdb.d/script_database.sql && java -jar app.jar"]
