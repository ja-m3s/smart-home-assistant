#Build the utility package
#This is only used to extract the metrics in circleci
FROM maven:3.9.6-eclipse-temurin-22 as build
WORKDIR /app
COPY sharedUtils sharedUtils
WORKDIR /app/sharedUtils
RUN ./gradlew :clean :check :javadoc :jar
# Zip app metrics
RUN apt update && apt install -y zip
WORKDIR /app/sharedUtils/build/
RUN zip -r metrics.zip docs
RUN zip -r metrics.zip reports