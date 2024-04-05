#!/usr/bin/env bash
# Name: helm-upgrade.sh
# Description: Upgrades system via helm
# Author: ja-m3s
RELEASE_NAME="${1:-"test-release"}"
microk8s helm3 upgrade "${RELEASE_NAME}" ../../helm

