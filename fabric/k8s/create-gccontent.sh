kubectl apply -f gccontent/
sleep 5
kubectl scale --replicas=$1 deploy/gccontent-agent
