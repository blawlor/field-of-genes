size=${1-12} #If not supplied, defaults to 12.
partitions=${2-32} #If not supplied, defaults to 32.

docker-compose -f docker-compose.yml -f digital_ocean.yml -p kafka up -d
docker-compose -f docker-compose.yml -f digital_ocean.yml -p kafka scale kafka=$size
. set-kafka-address.sh
./create-topics.sh $partitions

