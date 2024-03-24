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

# Define array containing image names and Dockerfile paths
declare -a images=(
    "event-bus ${SCRIPT_DIR}/../event-bus/Dockerfile"
    "event-bus-connector ${SCRIPT_DIR}/../event-bus-connector/Dockerfile"
    "producer ${SCRIPT_DIR}/../producer/Dockerfile"
    "consumer ${SCRIPT_DIR}/../consumer/Dockerfile"
    "database ${SCRIPT_DIR}/../database/Dockerfile"
)

# Function to build, tag, push, and pull Docker images
build_and_push_image() {
    local image_name="$1"
    local dockerfile_path="$2"
    
    # Build image
    docker build -t "${image_name}" --file "${dockerfile_path}" "$(dirname "${dockerfile_path}")"
    
    # Tag image
    docker tag "${image_name}" "${REGISTRY}:${REGISTRY_PORT}/${image_name}"
    
    # Push image to registry
    docker push "${REGISTRY}:${REGISTRY_PORT}/${image_name}"
    
    # Pull image from registry
    docker pull "${REGISTRY}:${REGISTRY_PORT}/${image_name}"
    
    # Remove local image
    docker image rm "${image_name}" "${REGISTRY}:${REGISTRY_PORT}/${image_name}" || true
}

# Loop through the array and build, tag, push, and pull images
for image in "${images[@]}"; do
    # shellcheck disable=2086
    build_and_push_image $image
done
