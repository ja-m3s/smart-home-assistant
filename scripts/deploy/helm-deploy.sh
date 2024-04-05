#!/usr/bin/env bash
# Name: helm-deploy.sh
# Description: Deploys system via helm
# Author: ja-m3s

RELEASE_NAME="${1:-"test-release"}"
microk8s helm3 install "${RELEASE_NAME}" ../../helm
