#!/usr/bin/env bash

#Docker
sudo apt install docker.io
sudo usermod -a -G docker $USER

#Microk8s
snap install microk8s --classic
sudo microk8s enable hostpath-storage registry rbac cert-manager dashboard
microk8s start
microk8s status
