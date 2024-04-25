# Grafana Application

## Brief Overview

This is a system which displays metrics from all the pods in the K8S cluster, using the metrics from Prometheus. It is deployed into the namespace called 
'monitoring'. A selection of reports have been built into the application to allow easy monitoring. These are reports for
cockroachDB and RabbitMQ. Three bespoke reports have been created for each of the java applications.

## How to build

This is not a built application.

## How to deploy

Deployment is via helm3 along with the other applications. This can be achieved by running:
```
./scripts/deploy/helm-apply.sh
```
## Technologies

The application uses the Grafana docker image.
