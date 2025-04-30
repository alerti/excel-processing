# Use a multi-stage build to reduce the final image size
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Use a minimal JRE image for the runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy only the built jar
COPY --from=build /app/target/*.jar app.jar

# Create a non-root user to run the application
RUN useradd -m appuser && \
    chown -R appuser:appuser /app
USER appuser

# Expose the port the app runs on
EXPOSE 8080

# Set the entry point
ENTRYPOINT ["java", "-jar", "app.jar"]
