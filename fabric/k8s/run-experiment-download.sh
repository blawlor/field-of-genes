size=${1-12} #If not supplied, defaults to 12.
partitions=${2-32} #If not supplied, defaults to 32.
files=${1-4} #If not supplied, defaults to 4.
threads=${2-4} #If not supplied, defaults to 4.
zone=us-east1-b
machine=n1-standard-8
#Repeat the following for different values of f (files) and t (threads)
#========================================================================================
gcloud container clusters create fieldofgenes --zone $zone --cluster-version 1.8.3-gke.0 --num-nodes $size --machine-type $machine --local-ssd-count 2 --image-type ubuntu

gcloud container clusters get-credentials fieldofgenes --zone $zone

kubectl run -i --tty loader-experiment --image=blawlor/loader-experiment --restart=Never -- $files $threads

gcloud container clusters delete -q fieldofgenes --zone $zone

#=================================================================================
