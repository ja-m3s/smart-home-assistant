#Build the utility package
FROM gradle:jdk-jammy as build
WORKDIR /app
COPY sharedUtils sharedUtils
COPY lightBulbMonitor lightBulbMonitor
COPY buildSrc buildSrc
COPY settings.gradle settings.gradle
RUN gradle sharedUtils:shadowJar
RUN gradle lightBulbMonitor:test :shadowJar :jacocoReport :javadoc

# Zip app metrics
RUN apt update && apt install -y zip
WORKDIR /app/lightBulbMonitor/build/
RUN zip -r metrics.zip reports
RUN zip -r metrics.zip docs

# Create production image
FROM eclipse-temurin:22_36-jre-alpine as production
COPY --from=build /app/lightBulbMonitor/build/libs/lightBulbMonitor-0.0.1-SNAPSHOT-all.jar /opt/app/app.jar
CMD ["java", "-jar", "/opt/app/app.jar"]
