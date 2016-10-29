#!/bin/bash
n=${1-32} #If not supplied, defaults to 32.
cd ../fabric
source ./set-kafka-address.sh
cd ../experiment
# Set the experiment going without any timing - just send the messages.
export start=$(date +%s)
docker run --rm blawlor/experiment $KAFKA_HOST:$KAFKA_PORT gccontent gccontent-instructions $n ignore
sleep 5 # Wait 5 seconds before trying to read the output queue
export nummessages=$(docker run -it --rm -e ZK_HOST=$ZK_HOST -e ZK_PORT=$ZK_PORT wurstmeister/kafka:0.10.0.0 /opt/kafka_2.11-0.10.0.0/bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list $KAFKA_HOST:$KAFKA_PORT --topic ref-seq-gccontent --time -1 --offsets 1 | awk -F  ":" '{sum += $3} END {print sum}')
echo Number of messages in output topic: $nummessages
while true
do
sleep 10
export newnummessages=$(docker run -it --rm -e ZK_HOST=$ZK_HOST -e ZK_PORT=$ZK_PORT wurstmeister/kafka:0.10.0.0 /opt/kafka_2.11-0.10.0.0/bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list $KAFKA_HOST:$KAFKA_PORT --topic ref-seq-gccontent --time -1 --offsets 1 | awk -F  ":" '{sum += $3} END {print sum}') 
echo $nummessages
if [ "$newnummessages" == "$nummessages" ]; then
  break;
fi
export nummessages=$newnummessages
done
export stop=$(date +%s)

let "time=($stop-$start)-10"

echo $time seconds
