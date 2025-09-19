# Build stage
FROM openjdk:21-jdk-slim AS build

WORKDIR /app

# Copy gradle files
COPY gradle gradle
COPY gradlew .
#COPY gradle.properties .
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Build application
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

# Runtime stage
FROM openjdk:21-jdk-slim

WORKDIR /app

# Install curl for health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Create logs, file upload and temp directories with proper permissions
RUN mkdir -p logs /flow/data /flow/temp && chown -R spring:spring logs /flow/data /flow/temp

USER spring:spring

# Copy JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]