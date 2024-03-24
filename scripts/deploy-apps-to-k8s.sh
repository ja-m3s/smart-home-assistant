#!/usr/bin/env bash
#   Name: deploy-apps-to-k8s.sh
#   Description: Applys applications to the K8S cluster
#   Notes: Tested on Fedora Silverblue only.
#   Author: ja-m3s

apply_kubernetes_manifest() {
    local manifest_file="$1"
    kubectl apply -f "${manifest_file}"
}

# Apply Kubernetes manifests
apply_kubernetes_manifest "../kubernetes/database-secret.yaml"
apply_kubernetes_manifest "../kubernetes/database-deployment.yaml"
apply_kubernetes_manifest "../kubernetes/database-service.yaml"
apply_kubernetes_manifest "../kubernetes/event-bus-deployment.yaml"
apply_kubernetes_manifest "../kubernetes/event-bus-service.yaml"
apply_kubernetes_manifest "../kubernetes/producer-deployment.yaml"
apply_kubernetes_manifest "../kubernetes/producer-service.yaml"
apply_kubernetes_manifest "../kubernetes/consumer-deployment.yaml"
apply_kubernetes_manifest "../kubernetes/consumer-service.yaml"
apply_kubernetes_manifest "../kubernetes/kafka-connect-deployment.yaml"
apply_kubernetes_manifest "../kubernetes/kafka-connect-service.yaml"