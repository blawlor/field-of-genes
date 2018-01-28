#!/bin/bash
if [ ! -f ./set-do-token.sh ]; then
    echo "Please create a set-do-token.sh file which exports a DO key value."
    exit 1
fi
size=${1-5} #If not supplied, defaults to 5.
workerbees=`expr ${size} - 1`
source ./set-do-token.sh
export DIGITALOCEAN_IMAGE=debian-8-x64
export DIGITALOCEAN_PRIVATE_NETWORKING=true
export DIGITALOCEAN_SIZE=8gb
export DIGITALOCEAN_REGION=nyc1

echo "Creating nodes"

docker-machine create -d digitalocean queenbee
for i in `seq 1 $workerbees`;
do
    docker-machine create -d digitalocean workerbee$i
done



echo "Setting firewall rules on nodes"
docker-machine ssh queenbee apt-get install ufw
docker-machine ssh queenbee ufw allow 22/tcp
docker-machine ssh queenbee ufw allow 2376/tcp
docker-machine ssh queenbee ufw allow 2377/tcp
docker-machine ssh queenbee ufw allow 7946/tcp
docker-machine ssh queenbee ufw allow 7946/udp
docker-machine ssh queenbee ufw allow 4789/udp
docker-machine ssh queenbee ufw --force enable
docker-machine ssh queenbee systemctl restart docker

for j in `seq 1 $workerbees`;
do
docker-machine ssh workerbee$j apt-get install ufw
docker-machine ssh workerbee$j ufw allow 22/tcp
docker-machine ssh workerbee$j ufw allow 2376/tcp
docker-machine ssh workerbee$j ufw allow 7946/tcp
docker-machine ssh workerbee$j ufw allow 7946/udp
docker-machine ssh workerbee$j ufw allow 4789/udp
docker-machine ssh workerbee$j ufw --force enable
docker-machine ssh workerbee$j systemctl restart docker
done

echo "Creating Swarm across nodes"
QUEEN_IP="$(docker-machine ip queenbee)"
docker-machine ssh queenbee sudo docker swarm init --advertise-addr ${QUEEN_IP}
SWARM_TOKEN=$(docker-machine ssh queenbee sudo docker swarm join-token -q worker)
echo "Queen ip is ${QUEEN_IP} and Swarm token is ${SWARM_TOKEN}"

for k in `seq 1 $workerbees`;
do
echo "docker-machine ssh workerbee$k sudo docker swarm join --token $SWARM_TOKEN $QUEEN_IP:2377"
docker-machine ssh workerbee$k sudo docker swarm join --token $SWARM_TOKEN $QUEEN_IP:2377
done


echo "Swarm creation complete. To use the swarm, run the following: "
echo "eval $(docker-machine env queenbee)"

