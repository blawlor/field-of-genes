#!/bin/bash
topic=$1
message_file_name=$2
number_of_messages=$3
result_topic=$4
export start=$(date +%s)
echo Starting anew at $start
#java -Xms512m -Xmx2048m -jar experiment.jar kafka-0.broker.kafka.svc.cluster.local:9092 $topic $message_file_name $number_of_messages $result_topic
sleep 5 # Wait 5 seconds before trying to read the output queue
#export nummessages=$(/opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server kafka-0.broker.kafka.svc.cluster.local:9092 --topic ref-seq-gccontent --time -1 --offsets 1 | awk -F  ":" '{sum += $3} END {print sum}')
echo Number of messages in output topic: $nummessages
while true
do
sleep 10 #Make this a variable
#export newnummessages=$(/opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server kafka-0.broker.kafka.svc.cluster.local:9092 --topic ref-seq-gccontent --time -1 --offsets 1 | awk -F  ":" '{sum += $3} END {print sum}')
echo $nummessages
if [ "$newnummessages" == "$nummessages" ]; then
  break;
fi
export nummessages=$newnummessages
done
export stop=$(date +%s)

let "time=($stop-$start)-10" #Note 10 = variable

echo $time seconds
