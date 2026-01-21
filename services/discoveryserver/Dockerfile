# Usamos una imagen de Maven con Java 21 para compilar el proyecto
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos el archivo de configuración de dependencias
COPY pom.xml .

# Descargamos las dependencias
RUN mvn dependency:go-offline

# Copiamos el código fuente y compilamos el .jar saltandonos los tests
COPY src ./src
RUN mvn clean package -DskipTests


# Usamos una imagen ligera de solo JRE (Java Runtime Environment)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiamos solo el archivo .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto estándar de Spring Boot (se mapeará en el docker-compose)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]