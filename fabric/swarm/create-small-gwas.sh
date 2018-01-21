#!/bin/bash
if [ ! -f ./set-do-token.sh ]; then
    echo "Please create a set-do-token.sh file which exports a DO key value."
    exit 1
fi
source ./set-do-token.sh
export DIGITALOCEAN_IMAGE=ubuntu-16-04-x64
export DIGITALOCEAN_PRIVATE_NETWORKING=true
export DIGITALOCEAN_SIZE=8gb
export DIGITALOCEAN_REGION=nyc1


docker-machine create -d digitalocean gwas
