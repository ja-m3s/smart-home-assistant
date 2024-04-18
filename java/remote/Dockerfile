#Build the utility package

FROM gradle:jdk-jammy as build
WORKDIR /app
COPY sharedUtils sharedUtils
COPY remote remote
COPY buildSrc buildSrc
COPY settings.gradle settings.gradle
COPY config config
RUN gradle sharedUtils:check test shadowJar jacocoReport javadoc
RUN gradle remote:test shadowJar jacocoReport javadoc

RUN apt update && apt install -y zip
RUN zip -r metrics.zip sharedUtils/build/reports
RUN zip -r metrics.zip sharedUtils/build/docs
RUN zip -r metrics.zip remote/build/reports
RUN zip -r metrics.zip remote/build/docs

# Create production image
FROM tomcat:jre21-temurin-jammy as production
COPY --from=build /app/remote/build/libs/remote-0.0.1-SNAPSHOT-plain.war /usr/local/tomcat/webapps/remote.war
