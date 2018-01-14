kubectl apply -f loader/
sleep 5
kubectl scale --replicas=$1 deploy/loader-agent

# Loop on following command until its equal to $1 
# kubectl get pods  | grep "loader-agent-" | grep Running | wc -l
