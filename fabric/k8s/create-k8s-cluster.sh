nodes=${1-1} #If not supplied, defaults to 1.
zone=us-east1-b
machine=n1-standard-8
#Repeat the following for different values of f (files) and t (threads)
#========================================================================================
gcloud container clusters create fieldofgenes --zone $zone --cluster-version 1.8.5-gke.0 --num-nodes $nodes --machine-type $machine --local-ssd-count 2 --image-type ubuntu

kubectl config set-context $(kubectl config current-context) --namespace kafka

kubectl apply -f configure/gke-storageclass-zookeeper-ssd.yml
kubectl apply -f configure/gke-storageclass-broker-pd.yml
sleep 5
kubectl apply -f zookeeper/
sleep 5
kubectl apply -f kafka/
sleep 5
./scale-kafka.sh $nodes

# Loop on this command until the result = $nodes
# ./get-all.sh | grep "po/kafka-" | grep Running | wc -l
