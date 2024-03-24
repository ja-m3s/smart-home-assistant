#!/usr/bin/env bash
#   Name: build-images.sh
#   Description: Builds all application images and puts them into the registry in env.sh
#   Author: ja-m3s
set -eux

# Define script directory
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

# Source environment variables
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/env.sh"

# Function to build and push Docker images
build_and_push_image() {
    local image_name="$1"
    local dockerfile_path="$2"
    
    # Build image
    docker build -t "${image_name}" --file "${dockerfile_path}" "${SCRIPT_DIR}/../${image_name}"
    
    # Tag image
    docker tag "${image_name}" "${REGISTRY}:${REGISTRY_PORT}/${image_name}"
    
    # Push image to registry
    docker push "${REGISTRY}:${REGISTRY_PORT}/${image_name}"
    
    # Remove local image
    docker image rm "${image_name}" "${REGISTRY}:${REGISTRY_PORT}/${image_name}" || true
}

# Build and push images
build_and_push_image "event-bus" "../event-bus/Dockerfile"
build_and_push_image "producer" "../producer/Dockerfile"
build_and_push_image "consumer" "../consumer/Dockerfile"
build_and_push_image "database" "../database/Dockerfile"
build_and_push_image "kafka-connect" "../kafka-connect/Dockerfile"

# Pull images from registry
docker pull "${REGISTRY}:${REGISTRY_PORT}/event-bus"
docker pull "${REGISTRY}:${REGISTRY_PORT}/producer"
docker pull "${REGISTRY}:${REGISTRY_PORT}/consumer"
docker pull "${REGISTRY}:${REGISTRY_PORT}/database"
