# Smart Home Assistant 

## Project Description

Please see [Smart Home Assistant Project brief](./Brief.pdf) for a project description.

## Components Description

- event-bus - a RabbitMQ messaging queue bus
- light-bulb - simple java program representing a light bulb
- light-bulb-monitor - a simple java program representing a light bulb monitor, sends a message to a light when it's been on for longer than necessary.
- cockroachdb - database - stores messages in a database.
- db_exporter - a simple java program which feeds from the event-bus and inserts records into the database.
- cockroachdb-secure-client - automatic schema deployment.

## Project Layout

- /docker - custom Dockerfiles
- /helm - deployment configuration
- /helm/raw-files - application configuration
- /java - the java component project files
- /scripts - helper scripts to aid with marshalling/unmarshalling/building the applications/admin
- /test-plans - manual test plans for inspecting that the environment has been created correctly

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

# Docker registry login

- The Java apps by default push to a private registy on Dockerhub. To configure this locally, run the script /scripts/manual-build-deploy/create-docker-secret to enable
helm to pull from this reply - you will need to have configured your docker repo already
in .docker folder of your home directory.

## CircleCI

- The project is configured to automatically build the java applications 
- To configure this yourself there are some scripts in the /scripts/circleci-build folder
- They may require some changes in order to pass in the required variables

## Jacoco

- There are code coverage tools deployed in the Java projects, the output is visible when running mvn clean verify inside each Java project folder or in the Artifact section of circleci

#Document docker login process for microk8s