docker-compose -f docker-compose.yml -f digital_ocean.yml -p kafka down
docker volume rm $(docker volume ls -qf dangling=true)
