docker-compose -f docker-compose.yml -f digital_ocean.yml -p kafka up -d
docker-compose -f docker-compose.yml -f digital_ocean.yml -p kafka scale kafka=12
. set-kafka-address.sh
./create-topics.sh

