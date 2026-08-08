# =========================================================
# Stage 1: Build the Spring Boot application
# =========================================================

FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom first so Maven dependencies can be cached.
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Copy source code.
COPY src ./src

# Build the application.
RUN mvn clean package -DskipTests -B


# =========================================================
# Stage 2: Run the application
# =========================================================

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user.
RUN addgroup -S spring && adduser -S spring -G spring

USER spring

# Copy the generated Spring Boot JAR.
COPY --from=build /app/target/shopsphere-0.0.1-SNAPSHOT.jar app.jar

# Application port.
EXPOSE 8080

# Start Spring Boot.
ENTRYPOINT ["java", "-jar", "app.jar"]