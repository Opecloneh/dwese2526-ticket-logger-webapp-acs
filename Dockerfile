# ------------------------------------------
# Stage 1: Builder
# Imagen con Maven y JDK 21 para compilar la aplicación
# ------------------------------------------
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app

# Copiamos pom.xml para descargar dependencias primero
COPY pom.xml .

# Copiamos el código fuente
COPY src ./src

# Compilamos y empaquetamos la aplicación sin tests
RUN mvn clean package -DskipTests

# ------------------------------------------
# Stage 2: Runtime
# Imagen ligera con solo JRE 21 para ejecutar la app
# ------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiamos el JAR generado desde el builder
COPY --from=builder /app/target/dwese2526-ticket-logger-webapp-acs-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto que usa Spring Boot (8080)
EXPOSE 8080

# Ejecutamos la app
ENTRYPOINT ["java", "-jar", "app.jar"]
