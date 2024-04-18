#Build the utility package
FROM gradle:jdk-jammy as build
WORKDIR /app
COPY sharedUtils sharedUtils
COPY dbImporter dbImporter
WORKDIR /app/sharedUtils
RUN gradle :shadowJar
WORKDIR /app/dbImporter
RUN gradle :check :test :shadowJar :jacocoReport :javadoc

# Zip app metrics
RUN apt update && apt install -y zip
WORKDIR /app/dbImporter/build/
RUN zip -r metrics.zip reports
RUN zip -r metrics.zip docs

# Create production image
FROM eclipse-temurin:22_36-jre-alpine as production
COPY --from=build /app/dbImporter/build/libs/dbImporter-0.0.1-SNAPSHOT-all.jar /opt/app/app.jar
CMD ["java", "-jar", "/opt/app/app.jar"]
