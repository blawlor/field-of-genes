#!/bin/bash
n=${1-32} #If not supplied, defaults to 32.
cd ../fabric
source ./set-kafka-address.sh
cd ../experiment
java -jar target/experiment-1.0-SNAPSHOT-jar-with-dependencies.jar $KAFKA_HOST:$KAFKA_PORT gccontent gccontent-instructions $n ref-seq-gccontent 10
