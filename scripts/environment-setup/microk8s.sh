#!/usr/bin/env bash
# Name: microk8s.sh
# Description: Installs, Uninstalls and grants access to the microk8s dashboard.
# Author: ja-m3s

install() {
    # Docker
    sudo apt install docker.io
    sudo usermod -a -G docker $USER

    # Microk8s
    snap install microk8s --classic
    sudo microk8s enable rbac 
    sudo microk8s enable cert-manager 
    sudo microk8s enable hostpath-storage 
    sudo microk8s enable registry
    sudo microk8s enable dashboard
    microk8s start
    microk8s kubectl apply -f dashboard-access.yaml
    echo 'YOU WILL NEED THIS TO ACCESS THE DASHBOARD:'
    microk8s kubectl get secret admin-user -n kube-system -o jsonpath={".data.token"} | base64 -d
}

uninstall() {
    microk8s stop
    sudo microk8s disable dashboard
    sudo microk8s disable registry
    sudo microk8s disable hostpath-storage
    sudo microk8s disable cert-manager
    sudo microk8s disable rbac
    snap remove microk8s
    sudo apt remove docker.io
    sudo groupdel docker
}

dashboard() {
    microk8s kubectl get secret admin-user -n kube-system -o jsonpath={".data.token"} | base64 -d
    xdg-open "http://localhost:8001/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:/proxy/"
    microk8s kubectl proxy
}

# Check for command-line arguments
if [[ "$1" == "install" ]]; then
    install
elif [[ "$1" == "uninstall" ]]; then
    uninstall
elif [[ "$1" == "dashboard" ]]; then
    dashboard
else
    echo "Usage: $0 [install|uninstall|dashboard]"
    exit 1
fi

