# Stage 1 — Build frontend React
FROM node:20-alpine AS web-build
WORKDIR /build/web
COPY web/package.json web/package-lock.json* ./
RUN npm install
COPY web/ ./
RUN npm run build

# Stage 2 — Build API Spring Boot (frontend embarqué)
FROM eclipse-temurin:21-jdk-alpine AS api-build
WORKDIR /build/api
COPY api/ ./
COPY --from=web-build /build/web/dist ./src/main/resources/static
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

# Stage 3 — Image de production
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=api-build /build/api/build/libs/AdminApi-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
