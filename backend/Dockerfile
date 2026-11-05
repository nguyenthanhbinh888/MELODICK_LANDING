# Multi-stage build for optimal image size and minimal memory footprint
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compile and package application
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: configure dedicated non-root user and pre-create writable upload directory
RUN addgroup -S appgroup && adduser -S appuser -G appgroup \
    && mkdir -p /app/uploads/artists \
    && chown -R appuser:appgroup /app

COPY --chown=appuser:appgroup --from=builder /app/target/melodick-recs-backend-1.0.0.jar app.jar

USER appuser

# Dynamic port binding from Render environment ($PORT)
ENV PORT=10000
# JVM optimizations tailored for Render Free 512MB RAM limitation
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx300m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC"

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]
