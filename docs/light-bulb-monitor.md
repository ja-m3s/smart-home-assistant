# Light Bulb Monitor Application

## Brief Overview

This is a Java application which listens for messages on the event bus. When a message from a light bulb is 
received, it will check to see how long a light-bulb has been on, if it's been on longer than the time out interval, the light bulb monitor sends a message
to the light bulb telling it to turn it's self off.

## How to build

This is built as part of the CircleCI pipeline, which will build the application, and push the created image to the repository marked in
your smart-home-org-context in CircleCI. A CircleCI artifact is also created as part of this process which contains code metrics.

It can also be build using the build script located in /scripts/build:
```
./build-images.sh
```
If you just want to build the jar file, rather than the docker image, then you will first need to build the sharedUtils jar with the following commands::
```
cd java
gradle sharedUtils:shadowJar
gradle lightBulbMonitor:shadowjar
```

## How to run and debug locally

Once you've added the sharedUtils.jar you can debug as with any other application. You will need to expose the event bus out
of K8S for it to start working as it usually would.

## How to test

The unit tests can be run with the command:
```
mvn test
```
A test plan for this application is located in the /test-plans/ directory.

## How to deploy

Deployment is via helm3 along with the other applications. This can be achieved by running:
```
./scripts/deploy/./helm-apply.sh
```
## Technologies

The application used Gradle and docker for building. 

It uses Prometheus client for providing metrics which gives access to 
standard JVM metrics and two bespoke metrics: sent messages and received messages.

It uses the RabbitMQ client library to communicate with the event bus.

The project is built with code metrics plugins to enable the creation of the site. These code metrics are: code coverage via Jacoco,
style checks via Checkstyle, programming linting via Findbugs and JavaDocs for documentation.

## Dependencies

A full listing of dependencies can be found in /java/lightBulbMonitor/pom.xml
