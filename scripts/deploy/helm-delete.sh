#!/usr/bin/env bash
# Name: helm-delete.sh
# Description: Unmarshalls system via helm 
# Author: ja-m3s

RELEASE_NAME="${1:-"test-release"}"
helm3 delete --timeout 900 "${RELEASE_NAME}"
