files=${1-1} #If not supplied, defaults to 4.
threads=${2-1} #If not supplied, defaults to 4.
zone=us-east1-b
machine=n1-standard-8
#========================================================================================
gcloud container clusters create fieldofgenes --zone $zone --cluster-version 1.8.5-gke.0 --num-nodes 1 --machine-type $machine --local-ssd-count 2 --image-type ubuntu

kubectl config set-context $(kubectl config current-context) --namespace benchmark

gcloud container clusters get-credentials fieldofgenes --zone $zone

kubectl apply -f benchmark

cd gccontent-benchmark
./gccontent-benchmark.sh $files $threads


#=================================================================================


