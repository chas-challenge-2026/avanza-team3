# ---------- Frontend build ----------
FROM node:22-alpine AS frontend-build

WORKDIR /frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

# ---------- Backend build ----------

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/AvanzaPortal/pom.xml .
RUN mvn dependency:resolve -q
COPY backend/AvanzaPortal/src ./src
COPY --from=frontend-build /frontend/dist ./src/main/resources/static
RUN mvn package -DskipTests -q

# ---------- Runtime ----------

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/avanza-portal-1.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]