#!/usr/bin/env bash
# Name: build-images.sh
# Description: Builds all application images
# Author: ja-m3s
set -eux

# Define script directory
SCRIPT_DIR=$(dirname "$(readlink -f "$0")")

#Change to a repo of your choosing
REPO=localhost:32000

# Build the python-custom image
docker build -t "${REPO}/python-custom:3.9.19-slim-bullseye"  "${SCRIPT_DIR}/../docker/python-custom"
docker push ${REPO}/python-custom:3.9.19-slim-bullseye

