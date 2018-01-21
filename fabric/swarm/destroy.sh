docker-machine rm -f queenbee
docker-machine rm -f $(docker-machine ls -q --filter "name=workerbee*")
