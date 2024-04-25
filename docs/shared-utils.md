# Shared Util library Application

## Brief Overview

This is a Java library used by the other java applications which contains shared functionality.

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
```

## How to run and debug locally

The library is meant to be ran and debug as part of the other Java applications.

## How to test

The unit tests can be run with the command:
```
gradle sharedUtils:test
```
A test plan for this application is located in the /test-plans/ directory.

## How to deploy

Deployment is via helm3 along with the other applications. This can be achieved by running:
```
./scripts/deploy/helm-apply.sh
```
## Technologies

The application used Gradle and docker for building. 

It uses Prometheus client for providing metrics which gives access to 
standard JVM metrics and two bespoke metrics: sent messages and received messages.

It uses the RabbitMQ client library to communicate with the event bus.

The project is built with code metrics plugins to enable the creation of the site. These code metrics are: code coverage via Jacoco,
style checks via Checkstyle, programming linting via Findbugs and JavaDocs for documentation.

## Dependencies

A full listing of dependencies can be found in /java/sharedUtils/pom.xml

