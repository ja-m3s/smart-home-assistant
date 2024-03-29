#!/usr/bin/env bash

apt install microk8s docker.io

microk8s enable hostpath-storage registry

sudo usermod -a -G docker user
