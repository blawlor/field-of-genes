size=${1-12} #If not supplied, defaults to 12.
partitions=${2-32} #If not supplied, defaults to 32.
kubectl apply -f ./zookeeper/
kubectl apply -f ./kafka/
#docker-machine ssh queenbee sudo docker network create -d overlay kafka_network
#docker stack deploy --compose-file docker-compose.yml kafka
#docker service scale kafka_kafka=${size}
#docker-compose -f docker-compose.yml -f digital_ocean.yml -p kafka up -d
#docker-compose -f docker-compose.yml -f digital_ocean.yml -p kafka scale kafka=$size
. set-kafka-address.sh
./create-topics.sh $partitions

