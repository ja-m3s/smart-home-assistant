#Build the utility package
FROM maven:3.9.6-eclipse-temurin-22 as utilsBuild
WORKDIR /app
COPY sharedUtils sharedUtils
WORKDIR /app/sharedUtils
RUN ls -lah
RUN mvn dependency:resolve
RUN mvn clean verify package site

#Then build the app
FROM maven:3.9.6-eclipse-temurin-22 as build
WORKDIR /app
COPY lightBulb lightBulb
WORKDIR /app/lightBulb
COPY --from=utilsBuild /app/sharedUtils/target/sharedUtils-0.0.1-SNAPSHOT-jar-with-dependencies.jar .
RUN mvn install:install-file -Dfile=sharedUtils-0.0.1-SNAPSHOT-jar-with-dependencies.jar -DgroupId=com.jam3s -DartifactId=sharedUtils -Dpackaging=jar -Dversion=0.0.1-SNAPSHOT
#-DgroupId=com.example -DartifactId=utils -Dversion=1.0 -Dpackaging=jar
RUN mvn dependency:resolve
RUN mvn clean verify package site

# Zip app metrics
RUN apt update && apt install -y zip
WORKDIR /app/lightBulb/target/
RUN zip -r metrics.zip jacoco-reports
RUN zip -r metrics.zip surefire-reports
RUN zip -r metrics.zip site

# Create production image
FROM eclipse-temurin:22_36-jre-alpine as production
COPY --from=build /app/lightBulb/target/lightBulb-0.0.1-SNAPSHOT-jar-with-dependencies.jar /opt/app/app.jar
CMD ["java", "-jar", "/opt/app/app.jar"]