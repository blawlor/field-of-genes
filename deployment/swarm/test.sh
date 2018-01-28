#!/bin/bash
if [ ! -f ./set-aws-creds.sh ]; then
    echo "Please create a set-aws-creds.sh file which exports a AWS key id and key value."
    exit 1
fi
size=${1-5} #If not supplied, defaults to 5.
workerbees=`expr ${size} - 1`

echo "Creating Swarm across nodes"
QUEEN_IP="$(docker-machine ssh queenbee ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}' )"
docker-machine ssh queenbee sudo docker swarm init --advertise-addr ${QUEEN_IP}
SWARM_TOKEN=$(docker-machine ssh queenbee sudo docker swarm join-token -q worker)
echo "Queen ip is ${QUEEN_IP} and Swarm token is ${SWARM_TOKEN}"

for k in `seq 1 $workerbees`;
do
docker-machine ssh workerbee$k sudo docker swarm join --token $SWARM_TOKEN $QUEEN_IP:2377
done


echo "Swarm creation complete. To use the swarm, run the following: "
echo "eval $(docker-machine env queenbee)"

