#Build the utility package
#This is only used to extract the metrics in circleci
FROM maven:3.9.6-eclipse-temurin-22 as build
WORKDIR /app
COPY sharedUtils sharedUtils
WORKDIR /app/sharedUtils
RUN mvn dependency:resolve
RUN mvn clean verify test package site
# Zip app metrics
RUN apt update && apt install -y zip
WORKDIR /app/sharedUtils/target/
RUN zip -r metrics.zip jacoco-reports
RUN zip -r metrics.zip surefire-reports
RUN zip -r metrics.zip site