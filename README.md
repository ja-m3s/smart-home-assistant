# Smart Home Assistant 

## Project Description

Please see [Smart Home Assistant Project brief]("Brief.pdf") for a project description.

## Project Layout

- /docker - custom docker configuration
- /helm - application configuration
- /scripts - helper scripts to aid with marshalling/unmarshalling/building the applications

## Technology Stack

- The producer and consumer programs use python.
- The event bus and event bus connectors utilize Apache Kafka.
- The database backend uses postgres.
- All sit upon a kubernetes cluster.

## Configuration

All configuration can be found within the Helm .yaml files, either in values.yaml or the config maps associated with each component.

## Setup

To run the application, you will need to:
- deploy a docker registry 
- change variable REPO in script 'build-images.sh' to point to that registry
- amend value eventBusConnector.image in /helm/values.yaml to point to your new repo (keep name of image the same)
- amend value producer.image in /helm/values.yaml to point to your new repo (keep name of image the same)
- amend value consumer.image in /helm/values.yaml to point to your new repo (keep name of image the same)
- deploy a kubernetes cluster and configure kubectl to point to it
- install the helm program
- run script helm-deploy.sh