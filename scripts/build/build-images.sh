#!/usr/bin/env bash
# Name: build-images.sh
# Description: Builds all application images, by default this will push to the local microk8s registry unless passed a registry as parameter
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

#Might need to login if using remote repo.
#docker login --username $DOCKER_USER --password $DOCKER_PASS

# Build the python-custom image

DB_IMPORTER_IMAGE="${REPO}/smart-home-assistant:db-importer-latest"
docker build -t "${DB_IMPORTER_IMAGE}" "${SCRIPT_DIR}/../../java/lightBulb"
docker push "${DB_IMPORTER_IMAGE}"

LIGHTBULB_IMAGE="${REPO}/smart-home-assistant:light-bulb-latest"
docker build -t "${LIGHTBULB_IMAGE}" "${SCRIPT_DIR}/../../java/lightBulb"
docker push "${LIGHTBULB_IMAGE}"

LIGHTBULB_MONITOR_IMAGE="${REPO}/smart-home-assistant:light-bulb-monitor-latest"
docker build -t "${LIGHTBULB_MONITOR_IMAGE}" "${SCRIPT_DIR}/../../java/lightBulb"
docker push "${LIGHTBULB_MONITOR_IMAGE}"
