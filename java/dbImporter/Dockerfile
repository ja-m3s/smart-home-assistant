#Build the utility package
FROM gradle:jdk-jammy as build
WORKDIR /app
COPY sharedUtils .
COPY dbImporter .
COPY buildSrc .
COPY settings.gradle .
COPY config .
RUN gradle sharedUtils:check test shadowJar jacocoReport javadoc
RUN gradle dbImporter:check test shadowJar jacocoReport javadoc

# Zip app metrics
RUN apt update && apt install -y zip
RUN zip -r metrics.zip sharedUtils/build/reports \
    sharedUtils/build/docs \
    dbImporter/build/reports \
    dbImporter/build/docs

# Create production image
FROM eclipse-temurin:22_36-jre-alpine as production
WORKDIR /opt/app
COPY --from=build /app/dbImporter/build/libs/dbImporter-0.0.1-SNAPSHOT-all.jar app.jar
CMD ["java", "-jar", "app.jar"]
