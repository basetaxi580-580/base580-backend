FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw --batch-mode verify

FROM eclipse-temurin:24-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
