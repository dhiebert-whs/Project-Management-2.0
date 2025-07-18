# Multi-stage Dockerfile for FRC Project Management System
# Optimized for production deployment with Spring Boot 3.2

# Build stage
FROM openjdk:21-jdk-slim AS builder

# Set working directory
WORKDIR /build

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy Maven files for dependency caching
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -Pprod -DskipTests

# Production stage
FROM openjdk:21-jre-slim AS production

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN groupadd -r frcapp && useradd -r -g frcapp frcapp

# Install required packages
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create directories
RUN mkdir -p /app/data /app/logs && \
    chown -R frcapp:frcapp /app

# Copy jar from build stage
COPY --from=builder /build/target/*.jar app.jar

# Change ownership
RUN chown frcapp:frcapp app.jar

# Switch to non-root user
USER frcapp

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Environment variables
ENV SPRING_PROFILES_ACTIVE=production
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]