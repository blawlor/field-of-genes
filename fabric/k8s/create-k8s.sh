
#Repeat the following for different values of f (files) and t (threads)
#========================================================================================
gcloud container clusters create fieldofgenes --zone europe-west1-b --cluster-version 1.8.3-gke.0 --num-nodes 1 --machine-type n1-standard-4 --local-ssd-count 1 --image-type ubuntu

gcloud container clusters get-credentials fieldofgenes --zone europe-west1-b


kubectl run -i --tty loader-benchmark --image=blawlor/loader-benchmark --restart=Never -- $f $t

gcloud container clusters delete -q fieldofgenes --zone europe-west1-b


#=================================================================================




kubectl delete pod loader-benchmark
kubectl run -i --tty loader-benchmark --image=blawlor/loader-benchmark --restart=Never -- 8 8
kubectl delete pod loader-benchmark
kubectl run -i --tty loader-benchmark --image=blawlor/loader-benchmark --restart=Never -- 12 12

gcloud container clusters delete fieldofgenes


kubectl run -i --tty benchmark --image=blawlor/gccontent-benchmark --restart=Never -- 4 4
kubectl run -i --tty benchmark --image=blawlor/gccontent-benchmark --restart=Never -- 8 8
kubectl run -i --tty benchmark --image=blawlor/gccontent-benchmark --restart=Never -- 12 12




kubectl create --filename zk.yml
kubectl create --filename zk-service.yml
kubectl create --filename kafka-service.yml
kubectl get service kafka-service
# $SERVICE_EXTERNAL_IP for the kafka advertised ip address should be set to the result of the previous command
kubectl create --filename kafka.yml

