# Prometheus Application

## Brief Overview

This is a system which collects metrics from all the pods in the K8S cluster. Prometheus is deployed into the namespace called 
'monitoring'. It's configured to query the K8S cluster to retrieve the names of pods to scrape. It also is configured to 
scrape cockroachDB and RabbitMQ.

## How to build

This is not a built application.

## How to deploy

Deployment is via helm3 along with the other applications. This can be achieved by running:
```
./scripts/deploy/helm-apply.sh
```
## Technologies

The application uses the Prometheus docker image.
