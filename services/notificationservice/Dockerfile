# Usamos una imagen de Maven con Java 17 para compilar el proyecto
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos el pom y el código
COPY pom.xml .
COPY src ./src

# Compilamos el .jar saltandonos los tests
# Utilizamos cache de Maven para acelerar builds posteriores
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -DskipTests


# Usamos una imagen ligera de solo JRE (Java Runtime Environment)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copiamos solo el archivo .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto estándar de Spring Boot (se mapeará en el docker-compose)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]