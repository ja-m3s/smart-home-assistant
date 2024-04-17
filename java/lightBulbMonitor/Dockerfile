#Build the utility package
FROM gradle:jdk-jammy as utilsBuild
WORKDIR /app
COPY sharedUtils sharedUtils
WORKDIR /app/sharedUtils
RUN gradle :shadowJar

#Then build the app
FROM gradle:jdk-jammy as build
WORKDIR /app
COPY lightBulbMonitor lightBulbMonitor
WORKDIR /app/lightBulbMonitor
COPY --from=utilsBuild /app/sharedUtils/build/libs/sharedUtils-0.0.1-SNAPSHOT-all.jar .
RUN gradle :check :test :shadowJar :jacocoReport :javadoc

# Zip app metrics
RUN apt update && apt install -y zip
WORKDIR /app/lightBulbMonitor/build/
RUN zip -r metrics.zip reports
RUN zip -r metrics.zip docs

# Create production image
FROM eclipse-temurin:22_36-jre-alpine as production
COPY --from=build /app/lightBulbMonitor/target/lightBulbMonitor-0.0.1-SNAPSHOT-all.jar /opt/app/app.jar
CMD ["java", "-jar", "/opt/app/app.jar"]
