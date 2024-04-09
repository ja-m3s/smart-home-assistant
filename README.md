# Smart Home Assistant 

## Table of Contents

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

- The java applications use the following technologies: Java, jUnit, Jacoco, Maven, Docker, SpotBugs, Checkstyle
- The event bus uses RabbitMQ
- The database uses cockroachDB
- Deployment is done via helm3
- CD Pipelines are creating using CircleCi
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

All configuration can be found within the helm folder Values.yaml files, or the config maps associated with each component. In the cases of the Java programs, some configuration is performed in the source code.

## Setup

### Local Cluster Deployment on Ubuntu
1. cd scripts && ./microk8s.sh install -- this installs a development kubernetes
2. ./build-images.sh -- build images and add to the repository
3. ./helm-deploy.sh -- deploy into K8S

### Remote Cluster Deployment
1. cd scripts
2. ./build-images <<your registry>>
3.  amend helm values.yaml and change registry to your <<your registry>> in Values.yaml
4. point kubectl to K8S cluster
5.  helm install "release-name" helm

## Credits: 

I'd like to acknowledge James Heggs for the design of the project brief and Roxy Stafford for coming to me with this opportunity to demonstrate my skill.
