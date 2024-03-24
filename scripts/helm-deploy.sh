#!/usr/bin/env bash
RELEASE_NAME="${1:-"test-release"}"

helm install "${RELEASE_NAME}" ../helm