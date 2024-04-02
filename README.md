# Smart Home Assistant 

## Project Description

Please see [Smart Home Assistant Project brief](./Brief.pdf) for a project description.

## Project Layout

- /docker - custom Dockerfiles
- /helm - deployment configuration
- /helm/raw-files - application configuration
- /scripts - helper scripts to aid with marshalling/unmarshalling/building the applications/admin

## Technology Stack

- The producer and consumer programs use python.
- The event bus uses RabbitMQ
- The database backend uses cockroachDB.
- All are designed to be deployed upon a kubernetes cluster via helm3.
- Helm is used to deploy the application

## Configuration

All configuration can be found within the Helm .yaml files, either in values.yaml or the config maps associated with each component.

## Setup

### Local Configuration on Ubuntu

### Remote Cluster Deployment
1. 
