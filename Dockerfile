# ------------------------------------------
# Stage 1: Builder
# Imagen con Maven y JDK 21 para compilar la aplicación
# ------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copiamos pom.xml para descargar dependencias primero
COPY pom.xml .

RUN mvn -q -e -B dependency:go-offline

# Copiamos el código fuente
COPY src ./src

# Compilamos y empaquetamos la aplicación sin tests
RUN mvn -q -e -B clean package -DskipTests

# ------------------------------------------
# Stage 2: Runtime
# Imagen ligera con solo JRE 21 para ejecutar la app
# ------------------------------------------
FROM eclipse-temurin:21-jre-jammy

RUN useradd -ms /bin/bash spring

WORKDIR /app

# Copiamos el JAR generado desde el builder
COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p /app/uploads && chown -R spring:spring /app

USER spring

# Exponemos el puerto que usa Spring Boot (8080)
EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Ejecutamos la app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
