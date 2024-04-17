#Build the utility package
FROM gradle:jdk-jammy as utilsBuild
WORKDIR /app
COPY sharedUtils sharedUtils
WORKDIR /app/sharedUtils
RUN ./gradlew :shadowJar

#Then build the app
FROM maven:3.9.6-eclipse-temurin-22 as build
WORKDIR /app
COPY dbImporter dbImporter
WORKDIR /app/dbImporter
COPY --from=utilsBuild /app/sharedUtils/build/libs/sharedUtils-0.0.1-SNAPSHOT-all.jar .
RUN mvn install:install-file -Dfile=sharedUtils-0.0.1-SNAPSHOT-all.jar -DgroupId=com.jam3s -DartifactId=sharedUtils -Dpackaging=jar -Dversion=0.0.1-SNAPSHOT
RUN mvn dependency:resolve
RUN mvn clean verify test package site

# Zip app metrics
RUN apt update && apt install -y zip
WORKDIR /app/dbImporter/target/
RUN zip -r metrics.zip jacoco-reports
RUN zip -r metrics.zip surefire-reports
RUN zip -r metrics.zip site

# Create production image
FROM eclipse-temurin:22_36-jre-alpine as production
COPY --from=build /app/dbImporter/target/dbImporter-0.0.1-SNAPSHOT-jar-with-dependencies.jar /opt/app/app.jar
CMD ["java", "-jar", "/opt/app/app.jar"]