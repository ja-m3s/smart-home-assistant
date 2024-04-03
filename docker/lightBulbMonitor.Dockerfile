FROM eclipse-temurin:21
RUN mkdir /opt/app
COPY target/lightBulbMonitor-0.0.1-SNAPSHOT-jar-with-dependencies.jar /opt/app/
CMD ["java", "-jar", "/opt/app/lightBulbMonitor-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]