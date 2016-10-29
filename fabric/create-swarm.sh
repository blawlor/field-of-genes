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

docker-machine create -d digitalocean --digitalocean-size=2gb kvstore
export KV_IP=$(docker-machine ssh kvstore 'ifconfig eth1 | grep "inet addr:" | cut -d: -f2 | cut -d" " -f1')
eval $(docker-machine env kvstore)
docker run -d \
      -p ${KV_IP}:8500:8500 \
      -h consul \
      --restart always \
      progrium/consul -server -bootstrap

docker-machine create -d digitalocean --swarm --swarm-master --swarm-discovery="consul://${KV_IP}:8500" --engine-opt="cluster-store=consul://${KV_IP}:8500"   --engine-opt="cluster-advertise=eth1:2376" queenbee
for i in `seq 1 $workerbees`;
do
    docker-machine create -d digitalocean --swarm --swarm-discovery="consul://${KV_IP}:8500" \
--engine-opt="cluster-store=consul://${KV_IP}:8500"  --engine-opt="cluster-advertise=eth1:2376" workerbee-$i
done
echo "eval $(docker-machine env --swarm queenbee)"

