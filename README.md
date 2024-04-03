# Smart Home Assistant 

## Project Description

Please see [Smart Home Assistant Project brief](./Brief.pdf) for a project description.

## Components Description

- event-bus - a RabbitMQ messaging queue bus
- light - a simple java program representing a 'light', it has a state of on and off.
- light monitor - a simple java program representing a light monitor, sends a message to a light when it's been on for longer than necessary.
- cockroachdb - database - stores messages in a database.
- db_exporter - a simple java program which feeds from the event-bus and inserts records into the database.
- cockroachdb-client - a single pod for database administration and automatic schema deployment.

## Project Layout

- /docker - custom Dockerfiles
- /helm - deployment configuration
- /helm/raw-files - application configuration
- /scripts - helper scripts to aid with marshalling/unmarshalling/building the applications/admin

## Technology Stack

- java, RabbitMQ, cockroachDB, kubernetes, helm3.

## Configuration

All configuration can be found within the Helm .yaml files, either in values.yaml or the config maps associated with each component.

## Setup

### Local Cluster Deployment on Ubuntu
1. cd scripts && ./microk8s.sh install -- this installs a development kubernetes
2. ./build-images.sh -- build images and add to the repository
3. ./helm-deploy.sh -- deploy into K8S

### Remote Cluster Deployment
1. cd scripts
2. ./build-images <<your registry>>
3.  amend helm values.yaml and change python.registry to your <<your registry>>
4. point kubectl to K8S cluster
5.  helm install "release-name" ../helm
