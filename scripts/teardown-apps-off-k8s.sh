#!/usr/bin/env bash
#   Name: teardown-apps-off-k8s.sh
#   Description: Teardown applications from the K8S cluster
#   Author: ja-m3s

set -eux

#Create all app components in k8s
kubectl delete -f ../kubernetes