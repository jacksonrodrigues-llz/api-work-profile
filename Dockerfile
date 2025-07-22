# Etapa de build com Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa final de runtime com OpenJDK
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copia o JAR final do build para o container
COPY --from=build /app/target/api-work-profile-*.jar app.jar

# Expõe a porta usada pelo Railway (via variável de ambiente)
EXPOSE 8080

# Define o ENTRYPOINT usando a porta dinâmica do Railway
ENTRYPOINT ["sh", "-c", "java -Dserver.port=$PORT -Dspring.profiles.active=railway -jar app.jar"]