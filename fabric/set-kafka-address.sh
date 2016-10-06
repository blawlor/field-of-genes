#! /bin/bash

kafka1=$(docker-compose -p kafka ps | grep kafka_kafka_1)
export KAFKA_HOST=$(echo $kafka1 | awk  '{print $4}' | awk -F':' '{print $1}')
export KAFKA_PORT=$(echo $kafka1 | awk  '{print $4}' | awk -F':' '{print $2}' | awk -F'-' '{print $1}')
echo Kafka host set to $KAFKA_HOST and port set to $KAFKA_PORT
