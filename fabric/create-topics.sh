#!/bin/bash
source ./set-kafka-address.sh
docker run --rm -e ZK_HOST=$ZK_HOST -e ZK_PORT=$ZK_PORT wurstmeister/kafka:0.10.0.0 /opt/kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper $ZK_HOST:$ZK_PORT --replication-factor 1 --partitions 24 --topic loader
docker run --rm -e ZK_HOST=$ZK_HOST -e ZK_PORT=$ZK_PORT wurstmeister/kafka:0.10.0.0 /opt/kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper $ZK_HOST:$ZK_PORT --replication-factor 1 --partitions 24 --topic gccontent
