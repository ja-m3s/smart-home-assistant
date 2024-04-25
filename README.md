# Smart Home Assistant 

## Introduction

The Smart Home Assistant is suite of applications which working together demonstrate a simple smart home IOT setup. Please see [Smart Home Assistant Project brief](./Brief.pdf) for a project description. The suite is comparted into the following components:

### light-bulb

The light-bulb java application aims to simulate a light bulb with auto-turnoff functionality.

### light-bulb-monitor

The light-bulb-monitor java application aims to simulate a monitor which turns off the light bulb after a set period of time.

### db-importer

The db-importer java application aims feeds messages from RabbitMQ into the CockroachDB database.

### rabbitmq

A scaleable, fault-tolerant event bus.

### cockroachdb

A scaleable, fault-tolerant database.

### cockroachdb-secure-client

An administration console for the database

### Prometheus

A logging and metrics gathering system.

### Grafana

A data visualization system.

## Technology Stack

- The Java applications use the following technologies: Java, jUnit, Jacoco, Gradle, Docker, SpotBugs, Checkstyle and JavaDocs
- The event bus uses RabbitMQ
- The database uses cockroachDB
- Deployment is done via helm3
- CD Pipelines are creating the java application images using CircleCi
- The infrastructure is Kubernetes
- Data visualization is done via Grafana
- Data metrics scraping is done via Prometheus

## Project Source Layout

- /helm - deployment configuration
- /helm/raw-files - application configuration
- /java - the java component project files
- /scripts - helper scripts to aid with marshalling/unmarshalling/building the applications/admin
- /test-plans - manual test plans for inspecting that the environment has been created correctly
- /docs - documentation

## Configuration

All configuration can be found within the helm folder Values.yaml files, or the config maps associated with each component. In the cases of the Java programs, some configuration is performed in the source code. The database schema is configured in helm/raw-files/.

## Setup

### Quick start

This will use the pre-built images in hub.docker.com

1. Installs a development kubernetes:
```
sudo ./scripts/environment-setup/microk8s.sh install 
```
2. Apply the application suite: 
```
./scripts/deploy/helm-apply-microk8s.sh
```

### Local Cluster Deployment on Ubuntu
1. Installs a development kubernetes:
```
sudo ./scripts/environment-setup/microk8s.sh install 
```
2. Build the images locally: 
```
./scripts/build/build-images.sh
```
3. Apply the application suite: 
```
./scripts/deploy/helm-apply-microk8s.sh
```

### Remote Cluster Deployment
1. Installs a development kubernetes:
```
sudo ./scripts/environment-setup/microk8s.sh install 
```
2. Build the images remotely: 
```
./scripts/build/build-images.sh 'YOUR REGISTRY NAME'
```
3. Amend helm values.yaml and change registry to your registry in helm/Values.yaml
4. Apply the application suite: 
```
./scripts/deploy/helm-apply.sh
```

## Repositories

The circleci pipeline pushes the build images into the following repositories:

- dockerjam3s/sma-remote
- dockerjam3s/sma-light-bulb
- dockerjam3s/sma-light-bulb-monitor
- dockerjam3s/sma-db-importer

## Credits: 

I'd like to acknowledge James Heggs for the Northcoders Cloud course and Roxy Stafford for coming to me with this opportunity to demonstrate my skills and RMing.
