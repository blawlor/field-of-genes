nodes=${1-12} #If not supplied, defaults to 12.
parallel=${2-32} #If not supplied, defaults to 32.
#Parallel: Number of partitions and number of agents.

# Create topics
export PARTITIONS=$parallel
envsubst < create-topics.yml | kubectl apply -f -

# Run loader container (set number of replicas)
kubectl run -i --tty loader-experiment --image=blawlor/loader-agent --env="KAFKA_HOST=kafka-0.broker.kafka.svc.cluster.local" --env="KAFKA_PORT=9092" --restart=Never 

#gcloud container clusters delete -q fieldofgenes --zone $zone

#=================================================================================
