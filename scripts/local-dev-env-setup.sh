#!/usr/bin/env bash

apt install microk8s docker.io

microk8s enable hostpath-storage registry rbac cert-manager

sudo usermod -a -G docker user
