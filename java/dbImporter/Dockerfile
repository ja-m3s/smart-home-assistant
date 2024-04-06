FROM maven:3.9.6-eclipse-temurin-22 as build
WORKDIR /app
RUN apt update && apt install -y zip
COPY pom.xml ./
RUN mvn dependency:resolve
COPY src ./src
RUN mvn clean verify package site
WORKDIR /app/target/
RUN zip -r metrics.zip jacoco-reports
RUN zip -r metrics.zip surefire-reports
RUN zip -r metrics.zip site
FROM eclipse-temurin:22_36-jre-alpine as production
COPY --from=build /app/target/dbImporter-0.0.1-SNAPSHOT-jar-with-dependencies.jar /opt/app/app.jar
CMD ["java", "-jar", "/opt/app/app.jar"]