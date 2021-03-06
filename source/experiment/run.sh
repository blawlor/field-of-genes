#!/bin/bash
topic=$1
message_file_name=$2
number_of_messages=$3
result_topic=$4
export start=$(date +%s)
echo Starting experiment at $start
java -Xms512m -Xmx2048m -jar experiment.jar kafka-0.broker.kafka.svc.cluster.local:9092 $topic $message_file_name $number_of_messages $result_topic
sleep 5 # Wait 5 seconds before trying to read the output queue
# --bootstrap-server bootstrap.kafka.svc.cluster.local:9092 --describe --group
# Prime the group by reading the first message
/opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server bootstrap.kafka.svc.cluster.local:9092 --from-beginning --max-messages=1 --topic ${result_topic} --consumer-property group.id=${result_topic}-group
export nummessages=$(/opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server bootstrap.kafka.svc.cluster.local:9092 --describe --group ${result_topic}-group | grep $result_topic  | awk '{sum += $4} END {print sum}')
echo Number of messages in output topic: $nummessages
while true
do
sleep 10 #Make this a variable
export newnummessages=$(/opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server kafka-0.broker.kafka.svc.cluster.local:9092 --describe --group ${result_topic}-group | grep $result_topic | awk '{sum += $4} END {print sum}')
echo $newnummessages
if [ "$newnummessages" == "$nummessages" ]; then
  sleep 10
  export newnummessages=$(/opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server kafka-0.broker.kafka.svc.cluster.local:9092 --describe --group ${result_topic}-group | grep $result_topic | awk '{sum += $4} END {print sum}')
  echo Rechecking: $newnummessages
  if [ "$newnummessages" == "$nummessages" ]; then
    break;
  fi
fi
export nummessages=$newnummessages
done
export stop=$(date +%s)

let "time=($stop-$start)-20" 

echo $time seconds
