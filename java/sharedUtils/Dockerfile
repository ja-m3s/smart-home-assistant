#Build the utility package
#This is only used to extract the metrics in circleci
FROM maven:3.9.6-eclipse-temurin-22 as build
WORKDIR /app
COPY sharedUtils sharedUtils
WORKDIR /app/sharedUtils
RUN mvn dependency:resolve
RUN mvn clean verify package site