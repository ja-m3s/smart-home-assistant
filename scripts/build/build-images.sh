#!/usr/bin/env bash
# Name: build-images.sh
# Description: Builds all application images, by default this will push to the local microk8s registry unless passed a registry as parameter
# Note: Might need to login if using remote repo.
# docker login --username $DOCKER_USER --password $DOCKER_PASS
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

REMOTE_IMAGE="${REPO}/sma-remote:latest"
docker build -t "${REMOTE_IMAGE}" -f "${SCRIPT_DIR}/../../java/remote/Dockerfile" "${SCRIPT_DIR}/../../java/" 
docker push "${REMOTE_IMAGE}"

DB_IMPORTER_IMAGE="${REPO}/sma-db-importer:latest"
docker build -t "${DB_IMPORTER_IMAGE}" -f "${SCRIPT_DIR}/../../java/dbImporter/Dockerfile" "${SCRIPT_DIR}/../../java/" 
docker push "${DB_IMPORTER_IMAGE}"

LIGHTBULB_IMAGE="${REPO}/sma-light-bulb:latest"
docker build -t "${DB_IMPORTER_IMAGE}" -f "${SCRIPT_DIR}/../../java/lightBulb/Dockerfile" "${SCRIPT_DIR}/../../java/" 
docker push "${LIGHTBULB_IMAGE}"

LIGHTBULB_MONITOR_IMAGE="${REPO}/sma-light-bulb-monitor:latest"
docker build -t "${DB_IMPORTER_IMAGE}" -f "${SCRIPT_DIR}/../../java/lightBulbMonitor/Dockerfile" "${SCRIPT_DIR}/../../java/" 
docker push "${LIGHTBULB_MONITOR_IMAGE}"
