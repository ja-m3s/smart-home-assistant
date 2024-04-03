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

microk8s ctr images rm $(microk8s ctr images ls name~='localhost:32000' | awk {'print $1'})

# Build the python-custom image
cd "${SCRIPT_DIR}/../../java/dbImporter"
mvn clean package
docker build -t "${REPO}/eclipse-temurin-db-importer:21" -f "${SCRIPT_DIR}/../../docker/dbImporter.Dockerfile" "${SCRIPT_DIR}/../../java/dbImporter"
docker push "${REPO}/eclipse-temurin-db-importer:21"

cd "${SCRIPT_DIR}/../../java/lightBulb"
mvn clean package
docker build -t "${REPO}/eclipse-temurin-light-bulb:21" -f "${SCRIPT_DIR}/../../docker/lightBulb.Dockerfile" "${SCRIPT_DIR}/../../java/lightBulb"
docker push "${REPO}/eclipse-temurin-light-bulb:21"

cd "${SCRIPT_DIR}/../../java/lightBulbMonitor"
mvn clean package
docker build -t "${REPO}/eclipse-temurin-light-bulb-monitor:21" -f "${SCRIPT_DIR}/../../docker/lightBulbMonitor.Dockerfile" "${SCRIPT_DIR}/../../java/lightBulbMonitor"
docker push "${REPO}/eclipse-temurin-light-bulb-monitor:21"
