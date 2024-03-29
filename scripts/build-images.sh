#!/usr/bin/env bash
# Name: build-images.sh
# Description: Builds all application images
# Author: ja-m3s
set -eux

# Define script directory
SCRIPT_DIR=$(dirname "$(readlink -f "$0")")

#Change to a repo of your choosing
REPO=localhost:32000

# Build the event-bus-connector image
#docker build -t "${REPO}/event-bus-connector" "${SCRIPT_DIR}/../docker/event-bus-connector"
#docker push ${REPO}/event-bus-connector:latest

# Build the python-custom image
docker build -t "${REPO}/python-custom"  "${SCRIPT_DIR}/../docker/python-custom"
docker push ${REPO}/python-custom:latest

