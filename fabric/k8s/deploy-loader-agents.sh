scale=${1-1}
kubectl apply -f loader
./scale-loader.sh $scale
echo Waiting for $scale instances of loader to be ready.
while true
do
sleep 10 
export instances=$(kubectl get pods | grep "loader-agent" | grep Running | wc -l)
if [ "$instances" == "$scale" ]; then
  echo $instances
  break;
else
  echo $instances
fi
done
