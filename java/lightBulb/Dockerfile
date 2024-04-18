#Build the utility package
FROM gradle:jdk-jammy as build
WORKDIR /app
COPY sharedUtils sharedUtils
COPY lightBulb lightBulb
COPY buildSrc buildSrc
COPY settings.gradle settings.gradle
RUN gradle sharedUtils:shadowJar
RUN gradle lightBulb:test :shadowJar :jacocoReport :javadoc

# Zip app metrics
RUN apt update && apt install -y zip
WORKDIR /app/lightBulb/build/
RUN zip -r metrics.zip reports
RUN zip -r metrics.zip docs

# Create production image
FROM eclipse-temurin:22_36-jre-alpine as production
COPY --from=build /app/lightBulb/build/libs/lightBulb-0.0.1-SNAPSHOT-all.jar /opt/app/app.jar
CMD ["java", "-jar", "/opt/app/app.jar"]
