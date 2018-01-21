scale=${1-1}
kubectl apply -f gccontent
./scale-gccontent.sh $scale
echo Waiting for $scale instances of gccontent-agent to be ready.
while true
do
sleep 10 
export instances=$(kubectl get pods | grep "gccontent-agent" | grep Running | wc -l)
if [ "$instances" == "$scale" ]; then
  echo $instances
  break;
else
  echo $instances
fi
done
