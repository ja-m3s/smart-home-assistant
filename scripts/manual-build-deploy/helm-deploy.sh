#!/usr/bin/env bash
RELEASE_NAME="${1:-"test-release"}"
microk8s helm3 install "${RELEASE_NAME}" ../../helm
