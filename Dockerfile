FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Baixa dependências primeiro (cache eficiente)
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/api-work-profile-0.0.1-SNAPSHOT.jar app.jar
EXPOSE $PORT
ENTRYPOINT ["java", "-Dspring.profiles.active=railway", "-jar", "app.jar"]