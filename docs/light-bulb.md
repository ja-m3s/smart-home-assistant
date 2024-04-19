# Light Bulb Application

## Brief Overview

This is a Java application which sends a message to the event bus when it's on. When a message from the light bulb monitor is 
received, it will check if the message is for this light bulb. If it is, the light bulb turns the light bulb off and stops
sending messages.

## How to build

This is built as part of the CircleCI pipeline, which will build the application, and push the created image to the repository marked in
your smart-home-org-context in CircleCI. A CircleCI artifact is also created as part of this process which contains code metrics.

It can also be build using the build script located in /scripts/build:
```
./build-images.sh
```
If you just want to build the jar file, rather than the docker image, then you will first need to add the sharedUtils jar to the 
maven repository with the following commands:
```
cd java
gradle sharedUtils:shadowJar
gradle lightBulb:shadowjar
```

## How to run and debug locally

Once you've added the sharedUtils.jar you can debug as with any other application. You will need to expose an event-bus outside of K8S for it to start working as it usually would.

## How to test

The unit tests can be run with the command:
```
mvn test
```
A test plan for this application is located in the /test-plans/ directory.

## How to deploy

Deployment is via helm along with the other applications. This can be achieved by running:
```
./scripts/deploy/./helm-apply.sh
```
## Technologies

The application used Maven and docker for building. 

It uses Prometheus client for providing metrics which gives access to 
standard JVM metrics and two bespoke metrics: sent messages and received messages.

It uses the RabbitMQ client library to communicate with the event bus.

The project is built with code metrics plugins to enable the creation of the site. These code metrics are: code coverage via Jacoco,
style checks via Checkstyle and programming linting via Findbugs. These

## Dependencies

A full listing of dependencies can be found in /java/lightBulb/pom.xml
