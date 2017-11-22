#!/bin/bash
if [ ! -f ./set-aws-creds.sh ]; then
    echo "Please create a set-aws-creds.sh file which exports a AWS key id and key value."
    exit 1
fi
size=${1-5} #If not supplied, defaults to 5.
workerbees=`expr ${size} - 1`
source ./set-do-token.sh
export DIGITALOCEAN_IMAGE=debian-8-x64
export DIGITALOCEAN_PRIVATE_NETWORKING=true
export DIGITALOCEAN_SIZE=8gb
export DIGITALOCEAN_REGION=nyc1

export AWS_DEFAULT_REGION=eu-west-1

echo "Creating nodes"

docker-machine create -d amazonec2 queenbee
for i in `seq 1 $workerbees`;
do
    docker-machine create -d amazonec2 workerbee$i
done



echo "Setting firewall rules on nodes"
docker-machine ssh queenbee sudo apt-get install ufw
docker-machine ssh queenbee sudo ufw allow 22/tcp
docker-machine ssh queenbee sudo ufw allow 2376/tcp
docker-machine ssh queenbee sudo ufw allow 2377/tcp
docker-machine ssh queenbee sudo ufw allow 7946/tcp
docker-machine ssh queenbee sudo ufw allow 7946/udp
docker-machine ssh queenbee sudo ufw allow 4789/udp
docker-machine ssh queenbee sudo ufw --force enable
docker-machine ssh queenbee sudo systemctl restart docker

for j in `seq 1 $workerbees`;
do
docker-machine ssh workerbee$j sudo apt-get install ufw
docker-machine ssh workerbee$j sudo ufw allow 22/tcp
docker-machine ssh workerbee$j sudo ufw allow 2376/tcp
docker-machine ssh workerbee$j sudo ufw allow 7946/tcp
docker-machine ssh workerbee$j sudo ufw allow 7946/udp
docker-machine ssh workerbee$j sudo ufw allow 4789/udp
docker-machine ssh workerbee$j sudo ufw --force enable
docker-machine ssh workerbee$j sudo systemctl restart docker
done

echo "Creating Swarm across nodes"
QUEEN_IP="$(docker-machine ip queenbee)"
docker-machine ssh queenbee sudo docker swarm init --advertise-addr ${QUEEN_IP}
SWARM_TOKEN=$(docker-machine ssh queenbee sudo docker swarm join-token -q worker)
echo "Queen ip is ${QUEEN_IP} and Swarm token is ${SWARM_TOKEN}"

for k in `seq 1 $workerbees`;
do
docker-machine ssh workerbee$k sudo docker swarm join --token $SWARM_TOKEN $QUEEN_IP:2377
done


echo "Swarm creation complete. To use the swarm, run the following: "
echo "eval $(docker-machine env queenbee)"

