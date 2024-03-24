#!/usr/bin/env bash
#   Name: env.sh
#   Description: Convenient place to put well known configuration
#   Author: ja-m3s

# shellcheck disable=SC2034
REGISTRY=127.0.0.1;
REGISTRY_PORT=5000;

function check_root(){
    if [ "$EUID" -ne 0 ]; then
        echo "This script must be run as root."
        exit 1
    fi
}