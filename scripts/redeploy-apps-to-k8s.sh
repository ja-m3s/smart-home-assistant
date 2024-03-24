#!/usr/bin/env bash
#   Name: redeploy-apps-k8s.sh
#   Description: A convenience script which calls other scripts
#   Tears down the apps in the cluster, rebuilds the images and redeploys them
#   Notes: Tested on Fedora Silverblue only.
#   Author: ja-m3s

set -eux

# Source environment variables
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
source "${SCRIPT_DIR}/env.sh"

#Check running as root
check_root

#Build and redeploy
./helm-delete.sh
./build-images.sh
./helm-deploy.sh

#Check pods are up
kubectl get pods
