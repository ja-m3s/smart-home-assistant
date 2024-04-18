#Build the utility package

FROM gradle:jdk-jammy as build
WORKDIR /app
COPY sharedUtils sharedUtils
COPY remote remote
WORKDIR /app/sharedUtils
RUN gradle :shadowJar
WORKDIR /app/remote
RUN gradle :war

# Create production image
FROM tomcat:jre21-temurin-jammy as production
COPY --from=build /app/remote/build/libs/remote-0.0.1-SNAPSHOT-plain.war /usr/local/tomcat/webapps/remote.war
