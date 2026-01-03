# --- Stage 1: Build ---
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
# Copy only the pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime ---
FROM openjdk:17-jdk-slim
WORKDIR /app
# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Standard port for Spring Boot
EXPOSE 8080

# Run the application with production flags
ENTRYPOINT ["java", "-jar", "app.jar"]