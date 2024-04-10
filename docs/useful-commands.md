# Useful Commands

## Overview

This document holds commands useful for the deployment and destroying of the suite of applications as well as other administrative tasks.

## Setup Local cluster
Setup microk8s
```
sudo ./scripts/environment-setup/microk8s.sh install
```
Uninstall microk8s
```
sudo ./scripts/environment-setup/microk8s.sh uninstall
```
View microk8s dashboard
```
sudo ./scripts/environment-setup/microk8s.sh dashboard
```
Create a docker repo secret for private repositorys. 
```
sudo ./scripts/environment-setup/create-docker-secret.sh
```
## Kubernetes
View pods
```
kubectl get pods
```
## Cockroach DB

Access the cockroachDB secure client for database administration:
```
./scripts/admin/database-admin-console.sh
```

## RabbitMQ

View cluster status
```
kubectl exec rabbitmq-0 -it -- bash
rabbitmqctl get_cluster_info
```

## Java Apps

Get a light bulb's logs:
```
kubectl logs light-bulb-0
```
Get a light bulb-monitor's logs:
```
kubectl logs light-bulb-monitor-0
```
Get a database importer's logs:
```
kubectl logs db-importer-0
```
