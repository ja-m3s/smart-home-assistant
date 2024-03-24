#!/usr/bin/env bash
#   Name: install-local-k8s-on-silverblue.sh
#   Description: Installs K8S on a local machine for development purposes
#   Notes: Tested on Fedora Silverblue only.
#   Author: ja-m3s

set -eux

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/env.sh"

# Check if the script is run as root
check_root

#Clear old configuration
mkdir -p /OLD_K8S_CONFIG
sudo mv /etc/kubernetes /OLD_K8S_CONFIG/ || true
sudo mv /var/lib/etcd /OLD_K8S_CONFIG/ || true
mkdir -p /etc/kubernetes /var/lib/etcd

#Disable the firewalling 
sudo systemctl disable --now firewalld

#Add necessary modules to load on startup
sudo cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF
sudo modprobe overlay
sudo modprobe br_netfilter

#Check loaded
lsmod | grep br_netfilter
lsmod | grep overlay

#Add sysctl values
sudo cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

#Apply sysctl
sudo sysctl --system

#Inspect sysctl to check set correctly
sysctl net.bridge.bridge-nf-call-iptables net.bridge.bridge-nf-call-ip6tables net.ipv4.ip_forward

#Install packages
rpm-ostree install cri-o containernetworking-plugins kubernetes kubernetes-kubeadm kubernetes-client --apply-live 

#Enable container software
sudo systemctl enable --now crio

#Pull the k8s base images
sudo kubeadm config images pull

#Enable k8s service
sudo systemctl enable --now kubelet

#Create the cluster
sudo kubeadm init --pod-network-cidr=10.244.0.0/16

#Setup kubectl for access to the local cluster for user
mkdir -p "${HOME}/.kube"
sudo cp -i /etc/kubernetes/admin.conf "${HOME}/.kube/config"
sudo chown "$(id -u):$(id -g)" "${HOME}/.kube/config"

#Permit pods to be created on the same node as the control plane
kubectl taint nodes --all node-role.kubernetes.io/control-plane-

#Add networking via Flannel
kubectl apply -f https://github.com/coreos/flannel/raw/master/Documentation/kube-flannel.yml

#Check working - should see some control plane pods
kubectl get pods --all-namespaces

#Fix the config map to remove 'loop' line, should fix coredns pods...
kubectl get configmap -n kube-system coredns -o json | jq '.data.Corefile |= gsub("loop\n";"")' | kubectl apply -f -

#write a oneliner which edits /etc/kubernetes/manifests/kube-controller-manager.yaml and replaces all references to /usr/libexec/kubernetes/kubelet-plugins/volume/exec with /opt/libexec/kubernetes/kubelet-plugins/volume/exec
sed -i 's@/usr/libexec/kubernetes/kubelet-plugins/volume/exec@/opt/libexec/kubernetes/kubelet-plugins/volume/exec@g' /etc/kubernetes/manifests/kube-controller-manager.yaml

#apply fixes by restarting the k8s service
systemctl restart kubelet