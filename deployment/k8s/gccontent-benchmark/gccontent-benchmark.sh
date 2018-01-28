f=${1-1} #Number of files. If not supplied, defaults to 1.
t=${2-1} # Number of threads. Defaults to 1.
export FILES=$f
export THREADS=$t
kubectl config set-context $(kubectl config current-context) --namespace benchmark
envsubst < gccontent-benchmark.yml | kubectl apply -f -

while true
do
sleep 10
export instances=$(kubectl get pods | grep "gccontent-benchmark" | grep Running | wc -l)
if [ "$instances" == "1" ]; then
  echo Container running
  break;
else
  echo .
fi
done

export podname=$(kubectl get pods | grep "gccontent-benchmark" | awk '{print $1}' )
kubectl logs -f $podname
