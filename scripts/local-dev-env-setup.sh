#!/usr/bin/env bash

#Docker
sudo apt install docker.io
sudo usermod -a -G docker $USER

#Microk8s
sudo apt install microk8s
microk8s enable hostpath-storage registry rbac cert-manager
microk8s start
microk8s status