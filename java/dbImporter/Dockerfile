FROM maven:3.9.6-eclipse-temurin-22 as build

WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:resolve
COPY src ./src
RUN mvn package

FROM eclipse-temurin:22_36-jre-jammy as production
COPY --from=build /app/target/dbImporter-0.0.1-SNAPSHOT-jar-with-dependencies.jar /opt/app/app.jar

CMD ["java", "-jar", "/opt/app/app.jar"]
