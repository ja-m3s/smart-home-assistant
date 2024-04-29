#!/usr/bin/env bash
# Name: helm-apply.sh
# Description: Deploys system via helm to local cluster
# Author: ja-m3s

# Define script directory
SCRIPT_DIR=$(dirname "$(readlink -f "$0")")

RELEASE_NAME="${1:-"test-release"}"
microk8s helm3 install --timeout 900s "${RELEASE_NAME}" "${SCRIPT_DIR}/../../helm"
