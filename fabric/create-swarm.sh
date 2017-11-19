#!/bin/bash
if [ ! -f ./set-do-token.sh ]; then
    echo "Please create a set-do-token.sh file which exports a Digital Ocean token."
    exit 1
fi
size=${1-5} #If not supplied, defaults to 5.
workerbees=`expr ${size} - 1`
source ./set-do-token.sh
export DIGITALOCEAN_IMAGE=debian-8-x64
export DIGITALOCEAN_PRIVATE_NETWORKING=true
export DIGITALOCEAN_SIZE=8gb
export DIGITALOCEAN_REGION=nyc1

docker-machine create -d digitalocean queenbee
for i in `seq 1 $workerbees`;
do
    docker-machine create -d digitalocean workerbee-$i
done
echo "eval $(docker-machine sh queenbee)"

