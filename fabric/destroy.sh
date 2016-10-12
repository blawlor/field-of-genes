docker-machine rm -f $(docker-machine ls -q --filter "swarm=queenbee")
docker-machine rm -f kvstore
