#!/usr/bin/env bash

set -eux
NAMESPACE=confluent

#MICROK8S=''; #set to this one if not using microk8s.
MICROK8S='microk8s'; #local dev env

# Check if MICROK8S is set to 'microk8s'
if [[ "${MICROK8S}" == 'microk8s' ]]; then
    # Enable hostPath storage using microk8s
    microk8s enable hostpath-storage
fi


#create a namespace in kubernetes
${MICROK8S} kubectl create namespace ${NAMESPACE}

#set ${NAMESPACE} as the default context
${MICROK8S} kubectl config set-context --current --namespace ${NAMESPACE}

#add the confluent repo to helm
${MICROK8S} helm repo add confluentinc https://packages.confluent.io/helm
${MICROK8S} helm repo update

#install it
${MICROK8S} helm upgrade --install \
  confluent-operator confluentinc/confluent-for-kubernetes \
  --set kRaftEnabled=true

#check working
${MICROK8S} kubectl get pods

${MICROK8S} kubectl apply -f confluent-platform.yaml

#check working
${MICROK8S} kubectl get pods

${MICROK8S} kubectl port-forward controlcenter-0 9021:9021