# Compilación
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar configuración y descargar dependencias para aprovechar la caché
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final de ejecución
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiar el jar generado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto del microservicio
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]