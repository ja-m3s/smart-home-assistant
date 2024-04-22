# Remote Application

## Brief Overview

This functions as a remote to turn on and off lightbulbs.

## How to build

This is built as part of the CircleCI pipeline, which will build the application, and push the created image to the repository marked in
your smart-home-org-context in CircleCI. A CircleCI artifact is also created as part of this process which contains code metrics.

It can also be build using the build script located in /scripts/build:
```
./build-images.sh
```
If you just want to build the jar file, rather than the docker image, then you will first need to add the sharedUtils jar to the
Gradle repository with the following commands:
```
cd java
gradle sharedUtils:shadowJar
gradle remote:war
```

## How to deploy

Deployment is via helm3 along with the other applications. This can be achieved by running:
```
./scripts/deploy/helm-apply.sh
```

## Access via browser

http://localhost:32760/remote/

