m=${1-1} #Number of messages. If not supplied, defaults to 1.
# Create topics
export NUMBER_OF_MESSAGES=$m
envsubst < loader-experiment.yml | kubectl apply -f -
sleep 5
export podname=$(kubectl get pods | grep "loader-experiment" | awk '{print $1}' )
kubectl logs -f $podname
