# CockroachDB Application

## Brief Overview

This is a system which functions as the database for the suite of applications. By default it operates with three pods as part of a cluster.

## How to build

This is not a built application.

## How to deploy

Deployment is via helm3 along with the other applications. This can be achieved by running:
```
./scripts/deploy/helm-apply.sh
```
## Technologies

The application uses the cockroachDB docker image.
