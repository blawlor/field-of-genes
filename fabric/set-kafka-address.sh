#! /bin/bash

kafka1=$(docker-compose -p kafka ps | grep kafka_kafka_1)
zk1=$(docker-compose -p kafka ps | grep kafka_zookeeper_1)
export KAFKA_HOST=$(echo $kafka1 | awk  '{print $4}' | awk -F':' '{print $1}')
export ZK_HOST=$(echo $zk1 | awk  '{print $7}' | awk -F':' '{print $1}')
export KAFKA_PORT=$(echo $kafka1 | awk  '{print $4}' | awk -F':' '{print $2}' | awk -F'-' '{print $1}')
export ZK_PORT=$(echo $zk1 | awk  '{print $7}' | awk -F':' '{print $2}' | awk -F'-' '{print $1}')
echo Kafka host set to $KAFKA_HOST and port set to $KAFKA_PORT
echo Zookeeper host set to $ZK_HOST and port set to $ZK_PORT
