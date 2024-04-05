# System Test

## Testing pre-requisites

In order to test the application you will need to deploy a Kubernetes cluster. For ease, I recommend doing this on a clean installation of Ubuntu. An Ubuntu 22.04 virtual machine was used to develop the system. To do this:

1. Install a fresh Ubuntu 22.04 onto a virtual machine
2. Check out the project:
```
git clone https://github.com/ja-m3s/smart-home-assistant
```
or copy the project zip file available in the Releases section of Github to an appropriate directory and unzip it:
```
unzip smart-home-assistant.zip
```
3. Install MicroK8S, a mini Kubernetes cluster using the project scripts:
```
cd smart-home-assistant/scripts
sudo ./microk8s.sh install
```
4. Start the cluster
```
microk8s start
```
6. Verify the cluster is installed correctly:
```
microk8s status
```
7. Login to hub.docker.com, this will create a credentials file in ~/.docker
```
docker login
```
7.   Configure microk8s to use the credentials for deployment of the image. The script pulls the credentials file from ~/.docker and creates a secret in microK8S
```
cd smart-home-assistant/scripts
./create-docker-secret.sh
```
10.   Deploy the applications to K8S via helm: 
```
cd smart-home-assistant/scripts/manual-build-deploy
./helm-deploy.sh
```
11. Verify the installation has been successful, it may take upto 10 minutes to fully deploy. All pods should be running/complete.
```
microk8s kubectl get pods
```

## Tests - Light Bulb

- Sends Messages containing details of the light bulb
- Receives messages from the light bulb monitor
- Turns the light off when it receives a message from the light bulb monitor

## Tests - Light Bulb Monitor

- Receives all light bulb messages
- Sends a message containg the light bulb hostname as 'target' when it detects a light bulb has been on longer than the light limit

## Tests - DBImporter

- Inserts all messages on the RabbitMQ exchange into the database (once)

## Tests Database

- Has messages written to the messages table on the table.

## Tests Prometheus

- Applied correctly to cluster

## Tests Grafana

- Applied correctly to cluster
- Prometheus is added as a datasource

## Tests Secure Client Cockroach

- Applys the schema to the database
- Can manipulate the database
