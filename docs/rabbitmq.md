# RabbitMQ Application

## Brief Overview

This is a system functions as the event bus for the suite of applications. By default it operates with three pods as part of a cluster, using the
k8s peer discovery plugin. Metrics are exposed using the Prometheus plugin.

## How to build

This is not a built application.

## How to deploy

Deployment is via helm3 along with the other applications. This can be achieved by running:
```
./scripts/deploy/./helm-deploy.sh
```
## Technologies

The application uses the RabbitMQ docker image.
