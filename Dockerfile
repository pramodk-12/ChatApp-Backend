# --- Stage 1: Build ---
# Use Maven with Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (better caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime ---
# Use JRE 21 for the final image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the generated JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Handle the PORT variable for cloud environments
ENTRYPOINT ["java", "-Dserver.port=${PORT:8080}", "-jar", "app.jar"]
