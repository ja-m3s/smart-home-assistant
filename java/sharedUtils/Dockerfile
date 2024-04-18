#Build the utility package
#This is only used to extract the metrics in circleci
FROM gradle:jdk-jammy as build
WORKDIR /app
COPY sharedUtils sharedUtils
COPY buildSrc buildSrc
COPY settings.gradle settings.gradle
RUN gradle :check :shadowJar :test :jacocoReport :javadoc
# Zip app metrics
RUN apt update && apt install -y zip
WORKDIR /app/sharedUtils/build/
RUN zip -r metrics.zip docs
RUN zip -r metrics.zip reports