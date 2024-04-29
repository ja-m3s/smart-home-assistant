#!/usr/bin/env bash
# Name: helm-upgrade.sh
# Description: Upgrades system via helm
# Author: ja-m3s

# Define script directory
SCRIPT_DIR=$(dirname "$(readlink -f "$0")")

RELEASE_NAME="${1:-"test-release"}"
helm3 upgrade --timeout 900s "${RELEASE_NAME}" "${SCRIPT_DIR}/../../helm"

