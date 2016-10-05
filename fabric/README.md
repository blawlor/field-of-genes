
Kafka Cluster on Swarm
============

In order to implement the Field of Genes proof of concept, we need to create the computation fabric and Kafka cluster that underpin it. This project gives the docker-machine and docker-compose instructions to create Field of Genes on Digital Ocean. There are two pre-requisites:

1. Docker installation (including docker-machine and docker-compose)
2. A DigitalOcean account (and an access token from that account)

##Steps
These steps assume you are using Linux, have cloned this project to a local directory, and are currently in this directory.
1. Create a file called `set-do-token.sh` which exports the value `DIGITALOCEAN_ACCESS_TOKEN` set to the value of your DigitalOcean access token (see [here](https://www.digitalocean.com/community/tutorials/how-to-use-the-digitalocean-api-v2) to create this token).
e.g.
```
#!/bin/bash
export DIGITALOCEAN_ACCESS_TOKEN=your token here
```
2. Run the create swarm script:
`./create-swarm.sh 3`
optionally passing the number of hosts you want in the swarm. Defaults to 5.
3. When step 2 has finished, switch to the swarm master:
`eval $(docker-machine env --swarm queenbee)`
 From now, all docker commands will be run against the swarm.
4. Deploy the Kafka cluster, including Zookeeper, using the provided docker-compose files:
`docker-compose -f docker-compose.yml -f digital_ocean.yml -p kafka up -d`
This creates a series of containers with a kafka_ prefix.
5. Scale the Kafka cluster up to the desired size. We typically want to create a broker-per-host. So assuming we created 5 hosts:
`docker-compose -f docker-compose.yml -f digital_ocean.yml -p kafka scale kafka=5`

If you want to see what the ip addresses of the brokers are, just run:
`docker-compose -f docker-compose.yml -f digital_ocean.yml -p kafka ps`

Your Kafka cluster on Swarm is ready to be used.

**You are being billed for your use of Digital Ocean**. Remember to clean up afterwards by simply removing the hosts:
`docker-machine rm $(docker-machine ls -q)` (this will prompt for confirmation and will remove **all** your docker hosts)


