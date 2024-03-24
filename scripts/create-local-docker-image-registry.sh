#!/usr/bin/env bash
#   Name: create-local-docker-image-registry.sh
#   Description: Creates a local docker image registry
#   Notes: Tested on Fedora Silverblue only.
#   Author: ja-m3s

set -eux

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source "${SCRIPT_DIR}/env.sh"

# Check if the script is run as root
check_root

DETACHED='-it'
#DETACHED='-d'

CN="${REGISTRY}"
O="Testing"
C="UK"
ST="Tyne & Wear"
L="Newcastle"
OU="Testing"
email="self-signed@test.com"

#Create a directory to be added to the registry docker
mkdir -p ../registry/certs

#Generate certificates
openssl genrsa 2048 > ../registry/certs/domain.key
chmod 400 ../registry/certs/domain.key
openssl req -new -x509 -nodes -sha256 -days 365 \
    -key "../registry/certs/domain.key" \
    -out "../registry/certs/domain.crt" \
    -subj "/C=${C}/ST=${ST}/L=${L}/O=${O}/OU=${OU}/CN=${CN}/emailAddress=${email}" \
    -addext "subjectAltName = DNS:${CN},IP:${CN}"

mkdir -p ~/.docker;
cat "../registry/certs/domain.crt" "../registry/certs/domain.key" > ~/.docker/ca.pem

#Prune old registry
docker container stop registry && docker container rm -v registry

#Build
docker build -t registry --file ../registry/Dockerfile ../registry

#Run registry
docker run ${DETACHED} --restart=always --name registry -p 5000:443 registry:latest
