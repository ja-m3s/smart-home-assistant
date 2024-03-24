#!/usr/bin/env bash
RELEASE_NAME="${1:-"test-release"}"

helm delete "${RELEASE_NAME}"