#Build the utility package

FROM gradle:jdk-jammy as build
WORKDIR /app
COPY sharedUtils sharedUtils
COPY remote remote
COPY buildSrc buildSrc
COPY settings.gradle settings.gradle
COPY config config
RUN gradle sharedUtils:shadowJar
RUN gradle remote:test shadowJar jacocoReport javadoc

# Create production image
FROM tomcat:jre21-temurin-jammy as production
COPY --from=build /app/remote/build/libs/remote-0.0.1-SNAPSHOT-plain.war /usr/local/tomcat/webapps/remote.war
