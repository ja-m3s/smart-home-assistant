#Build the utility package
FROM gradle:jdk-jammy as utilsBuild
WORKDIR /app
COPY sharedUtils sharedUtils
WORKDIR /app/sharedUtils
RUN ./gradlew :shadowJar

#Then build the app
FROM gradle:jdk-jammy as build
WORKDIR /app
COPY remote remote
WORKDIR /app/remote
COPY --from=utilsBuild /app/sharedUtils/build/libs/sharedUtils-0.0.1-SNAPSHOT-all.jar .
RUN ./gradlew :war

# Create production image
FROM tomcat:jre21-temurin-jammy as production
COPY --from=build /app/remote/build/libs/remote-0.0.1-SNAPSHOT-plain.war /usr/local/tomcat/webapps/remote.war
