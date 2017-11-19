docker-machine rm -f queeenbee
docker-machine rm -f $(docker-machine ls -q --filter "name=workerbee*")
