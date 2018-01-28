size=${1-32} #If not supplied, defaults to 32.

docker-compose -f docker-compose.yml -p fog up -d
docker-compose -f docker-compose.yml -p fog scale loader=$size
