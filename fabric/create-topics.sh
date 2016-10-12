#!/bin/bash
source ./set-kafka-address.sh
docker run --rm -e ZK_HOST=$ZK_HOST -e ZK_PORT=$ZK_PORT wurstmeister/kafka:0.10.0.0 /opt/kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper $ZK_HOST:$ZK_PORT --replication-factor 1 --partitions 32 --topic loader
docker run --rm -e ZK_HOST=$ZK_HOST -e ZK_PORT=$ZK_PORT wurstmeister/kafka:0.10.0.0 /opt/kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper $ZK_HOST:$ZK_PORT --replication-factor 1 --partitions 32 --topic gccontent
docker run --rm -e ZK_HOST=$ZK_HOST -e ZK_PORT=$ZK_PORT wurstmeister/kafka:0.10.0.0 /opt/kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper $ZK_HOST:$ZK_PORT --replication-factor 1 --partitions 48 --topic ref-seq
docker run --rm -e ZK_HOST=$ZK_HOST -e ZK_PORT=$ZK_PORT wurstmeister/kafka:0.10.0.0 /opt/kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper $ZK_HOST:$ZK_PORT --replication-factor 1 --partitions 48 --topic ref-seq-gccontent
