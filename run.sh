m=${1-12} #If not supplied, defaults to 12.
n=${2-16} #If not supplied, defaults to 16.
cd fabric
./up.sh $m $n
. set-kafka-address.sh
cd ../loader-agent
./up.sh $n
sleep 5
cd ../experiment
./load.sh $n
cd ../loader-agent
./down.sh
cd ../gccontent-agent
./up.sh $n
cd ../experiment
./run.sh $n

