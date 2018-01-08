m=${1-1} #Number of messages. If not supplied, defaults to 1.
# Create topics
export NUMBER_OF_MESSAGES=$m
envsubst < experiment.yml | kubectl apply -f -

