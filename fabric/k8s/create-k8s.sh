gcloud container clusters create fieldofgenes --zone europe-west1-b
gcloud container clusters get-credentials fieldofgenes --zone europe-west1-b
kubectl create --filename zk.yml
kubectl create --filename zk-service.yml
kubectl create --filename kafka-service.yml
kubectl get service kafka-service
# $SERVICE_EXTERNAL_IP for the kafka advertised ip address should be set to the result of the previous command
kubectl create --filename kafka.yml

