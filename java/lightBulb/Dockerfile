FROM maven:3.9.6-eclipse-temurin-22 as build
WORKDIR /app
RUN apt update && apt install -y zip
COPY pom.xml ./
RUN mvn dependency:resolve
COPY src ./src
RUN mvn clean package
WORKDIR /app/target/site/
RUN zip -r jacoco.zip jacoco
FROM eclipse-temurin:22_36-jre-alpine as production
COPY --from=build /app/target/lightBulb-0.0.1-SNAPSHOT-jar-with-dependencies.jar /opt/app/app.jar
CMD ["java", "-jar", "/opt/app/app.jar"]