#!/bin/bash
if [ ! -f ./set-do-token.sh ]; then
    echo "Please create a set-do-token.sh file which exports a Digital Ocean token."
    exit 1
fi
source ./set-do-token.sh
export DIGITALOCEAN_IMAGE=debian-8-x64
export DIGITALOCEAN_PRIVATE_NETWORKING=true
export DIGITALOCEAN_SIZE=8gb
export DIGITALOCEAN_REGION=lon1

docker-machine create -d digitalocean benchmark
echo "eval $(docker-machine env benchmark)"

