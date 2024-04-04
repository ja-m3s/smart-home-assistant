#!/usr/bin/env bash
# Name: build-images.sh
# Description: Builds all application images
# Author: ja-m3s
set -eux

# Define script directory
SCRIPT_DIR=$(dirname "$(readlink -f "$0")")

# Use the passed-in REPO variable if provided, otherwise fallback to default value
REPO="${1:-localhost:32000}"

# If no argument is passed, print out the default repository and a message indicating that an argument can be passed
if [ $# -eq 0 ]; then
    echo "No repository argument provided. Using default repository: ${REPO}"
    echo "You can pass a custom repository as an argument. Example: ./build-images.sh mycustomrepo:5000"
fi

#docker login --username $DOCKER_USER --password $DOCKER_PASS

# Build the python-custom image
docker build -t "${REPO}/eclipse-temurin-db-importer:latest" "${SCRIPT_DIR}/../../java/dbImporter"
docker push "${REPO}/eclipse-temurin-db-importer:latest"

docker build -t "${REPO}/eclipse-temurin-light-bulb:latest" "${SCRIPT_DIR}/../../java/lightBulb"
docker push "${REPO}/eclipse-temurin-light-bulb:latest"

docker build -t "${REPO}/eclipse-temurin-light-bulb-monitor:latest" "${SCRIPT_DIR}/../../java/lightBulbMonitor"
docker push "${REPO}/eclipse-temurin-light-bulb-monitor:latest"
