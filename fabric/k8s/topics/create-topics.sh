parallel=${1-32} #If not supplied, defaults to 32.
#Parallel: Number of partitions and number of agents.

# Create topics
export PARTITIONS=$parallel
envsubst < create-topics.yml | kubectl apply -f -

