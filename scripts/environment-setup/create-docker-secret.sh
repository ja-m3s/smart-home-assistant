##!/usr/bin/env bash
# Name: create-docker-secret.sh
# Description: Creates a docker secret to be used by microK8S when deploying with helm
# Enables access to a private repository
# Author: ja-m3s

# Copy Docker configuration file to current directory
cp ~/.docker/config.json .

# Encode Docker configuration file to base64 and store it in a variable
DOCKER_CONFIG_BASE64=$(base64 -w 0 config.json)

# Define the Kubernetes Secret manifest with the Docker config stored in a variable
CONFIG="
apiVersion: v1
kind: Secret
metadata:
  name: registry-credentials
type: kubernetes.io/dockerconfigjson
data:
  .dockerconfigjson: |-
    ${DOCKER_CONFIG_BASE64}
"

# Apply the Secret manifest using kubectl
echo "${CONFIG}" | microk8s kubectl apply -f -
