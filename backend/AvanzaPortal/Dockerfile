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
RUN apt-get update \
    && apt-get install -y --no-install-recommends build-essential ca-certificates cmake libssl-dev perl pkg-config zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*
COPY native /native
RUN cmake -S /native/risk -B /tmp/native-risk -DCMAKE_BUILD_TYPE=Release -DRISK_BUILD_TESTS=OFF \
    && cmake --build /tmp/native-risk \
    && cmake -S /native/fx -B /tmp/native-fx -DCMAKE_BUILD_TYPE=Release -DFX_BUILD_TESTS=OFF \
    && cmake --build /tmp/native-fx
COPY backend/AvanzaPortal/pom.xml .
RUN mvn dependency:resolve -q
COPY backend/AvanzaPortal/src ./src
RUN mkdir -p ./src/main/resources/linux-x86-64 \
    && cp /tmp/native-risk/bin/librisk.so ./src/main/resources/linux-x86-64/librisk.so \
    && cp /tmp/native-fx/bin/libfx.so ./src/main/resources/linux-x86-64/libfx.so
COPY --from=frontend-build /frontend/dist ./src/main/resources/static
RUN mvn package -DskipTests -q

# ---------- Runtime ----------

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates libssl3 zlib1g \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/avanza-portal-1.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
