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

### Local Cluster Deployment on Ubuntu
1. cd scripts && ./microk8s.sh -- this installs a development kubernetes
2. ./build-images.sh -- build images and add to the repository
3. ./helm-deploy.sh -- deploy into K8S

### Remote Cluster Deployment
1. cd scripts
2. ./build-images <<your registry>>
3.  amend helm values.yaml and change python.registry to your <<your registry>>
4. point kubectl to K8S cluster
5.  helm install "release-name" ../helm
