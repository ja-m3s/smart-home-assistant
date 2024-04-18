#Build the utility package
FROM gradle:jdk-jammy as build
WORKDIR /app
COPY sharedUtils sharedUtils
COPY lightBulbMonitor lightBulbMonitor
COPY buildSrc buildSrc
COPY settings.gradle settings.gradle
COPY config config
RUN gradle sharedUtils:check test shadowJar jacocoReport javadoc
RUN gradle lightBulbMonitor:check test shadowJar jacocoReport javadoc

# Zip app metrics
RUN apt update && apt install -y zip
RUN zip -r metrics.zip sharedUtils/build/reports
RUN zip -r metrics.zip sharedUtils/build/docs
RUN zip -r metrics.zip lightBulbMonitor/build/reports
RUN zip -r metrics.zip lightBulbMonitor/build/docs

# Create production image
FROM eclipse-temurin:22_36-jre-alpine as production
COPY --from=build /app/lightBulbMonitor/build/libs/lightBulbMonitor-0.0.1-SNAPSHOT-all.jar /opt/app/app.jar
CMD ["java", "-jar", "/opt/app/app.jar"]
