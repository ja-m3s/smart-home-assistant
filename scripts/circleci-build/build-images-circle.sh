#!/usr/bin/env bash
# Name: build-images-circle.sh
# Description: Builds all application images
# Author: ja-m3s
set -eux

# Define script directory
SCRIPT_DIR=$(dirname "$(readlink -f "$0")")

REPO="${DOCKER_REPO}"

docker login --username ${DOCKER_USER} --password ${DOCKER_PASSWORD}

# Build the python-custom image
docker build -t "${REPO}:db-importer-latest" "${SCRIPT_DIR}/../../java/dbImporter"
docker push "${REPO}:db-importer-latest"

docker build -t "${REPO}:light-bulb-latest" "${SCRIPT_DIR}/../../java/lightBulb"
docker push "${REPO}:light-bulb-latest"

docker build -t "${REPO}:light-bulb-monitor-latest" "${SCRIPT_DIR}/../../java/lightBulbMonitor"
docker push "${REPO}:light-bulb-monitor-latest"
