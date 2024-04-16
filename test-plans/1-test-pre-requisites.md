# Testing Prerequisites

In order to test the application you will need to deploy the application to a Kubernetes cluster. For ease, I recommend doing this on a clean installation of Ubuntu. An Ubuntu 22.04 virtual machine was used to develop the system. To do this:

1. Install a fresh Ubuntu 22.04 in the usual way onto a virtual machine, Gnome Boxes was used to develop the system, and is the simplest solution
2. Install git:
```
apt update && apt install git
```
3. Check out project:
```
git clone https://github.com/ja-m3s/smart-home-assistant
```
or copy the project zip file available in the Releases section of Github to an appropriate directory and unzip it:
```
unzip smart-home-assistant.zip
```
4. Install MicroK8S, a mini Kubernetes cluster using the project scripts:
```
cd smart-home-assistant/scripts
sudo ./microk8s.sh install
```
5. Start the cluster:
```
microk8s start
```
6. Verify the cluster is installed correctly:
```
microk8s status
```
7. Login to hub.docker.com, this will create a credentials file in ~/.docker:
```
docker login
```
8. Configure microk8s to use the credentials for deployment of the image. The script pulls the credentials file from ~/.docker and creates a secret in microK8S:
```
cd smart-home-assistant/scripts
./create-docker-secret.sh
```
9. Build the images, make sure to add your registry as an argument to the script:
```
apt update && apt install maven
cd scripts
./build-images-manual.sh <<Your registry on hub.docker.com>>
```
10. Deploy the applications to K8S via helm: 
```
cd smart-home-assistant/scripts/manual-build-deploy
./helm-apply.sh
```
11. Verify the installation has been successful, it may take upto 10 minutes to fully deploy. All pods should be running/complete.
```
microk8s kubectl get pods
```
