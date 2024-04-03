#!/usr/bin/env bash
RELEASE_NAME="${1:-"test-release"}"
microk8s helm3 delete "${RELEASE_NAME}"
