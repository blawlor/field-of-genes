#!/bin/bash
if [ ! -f ./set-google-project.sh ]; then
    echo "Please create a set-google-project.sh file which exports a Google Project name."
    exit 1
fi
source ./set-google-project.sh

export GOOGLE_ZONE=us-east1-b
export GOOGLE_MACHINE_TYPE=n1-standard-8
export GOOGLE_DISK_TYPE=pd-ssd
export GOOGLE_DISK_SIZE=750
docker-machine create -d google benchmark
echo "eval $(docker-machine env benchmark)"

