#!/usr/bin/env bash
# Name: helm-delete.sh
# Description: Unmarshalls system via helm from microk8s
# Author: ja-m3s

RELEASE_NAME="${1:-"test-release"}"
microk8s helm3 delete --timeout 900 "${RELEASE_NAME}"
