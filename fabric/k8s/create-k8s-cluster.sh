nodes=${1-12} #If not supplied, defaults to 12.
parallel=${2-32} #If not supplied, defaults to 32.
#Parallel: Number of partitions and number of agents.
zone=us-east1-b
machine=n1-standard-8
#Repeat the following for different values of f (files) and t (threads)
#========================================================================================
gcloud container clusters create fieldofgenes --zone $zone --cluster-version 1.8.5-gke.0 --num-nodes $nodes --machine-type $machine --local-ssd-count 2 --image-type ubuntu

#gcloud container clusters get-credentials fieldofgenes --zone $zone

#Create kafka now by applying the kafka files and then scaling according to the number of nodes.

#kubectl apply -f configure/gke-storageclass-zookeeper-ssd.yml

#kubectl apply -f configure/gke-storageclass-broker-pd.yml

#kubectl apply -f ./zookeeper/

#kubectl apply -f ./kafka/

#Run the experiments based on the supplied parallel factor.
#kubectl --namespace kafka scale statefulset kafka --replicas=$parallel

# Create topics
#export PARTITIONS=$parallel
#envsubst < create-topics.yml | kubectl apply -f -

# Run loader container (set number of replicas)
#kubectl run -i --tty loader-experiment --image=blawlor/loader-agent --env="KAFKA_HOST=kafka-0.broker.kafka.svc.cluster.local" --env="KAFKA_PORT=9092" --restart=Never 

#gcloud container clusters delete -q fieldofgenes --zone $zone

#=================================================================================
