Creating a Docker Swarm on DigitalOcean
=======================================

Note: This is taken from [this site](https://42notes.wordpress.com/2015/04/15/create-manage-a-docker-swarm-cluster-on-digital/) but then superceded by [this one](http://nathanleclaire.com/blog/2015/11/17/seamless-docker-multihost-overlay-networking-on-digitalocean-with-machine-swarm-and-compose-ft.-rethinkdb/)

## **Set the Digital Ocean token and settings**

```` bash
export DIGITALOCEAN_ACCESS_TOKEN="whatever"
export DIGITALOCEAN_IMAGE=debian-8-x64
export DIGITALOCEAN_PRIVATE_NETWORKING=true
export DIGITALOCEAN_SIZE=2gb
export DIGITALOCEAN_REGION=lon1
````

## **Create a key-value server for consul**

````bash
docker-machine create -d digitalocean kvstore
````

## **Then set a variable to be the ip of the kvstore**

``` bash
export KV_IP=$(docker-machine ssh kvstore 'ifconfig eth1 | grep "inet addr:" | cut -d: -f2 | cut -d" " -f1')
```

## **Run Consul on kvstore**

```bash
eval $(docker-machine env kvstore)
docker run -d \
      -p ${KV_IP}:8500:8500 \
      -h consul \
      --restart always \
      progrium/consul -server -bootstrap
```

## **Create a swarm master on DO**

```bash
docker-machine create -d digitalocean --swarm --swarm-master --swarm-discovery="consul://${KV_IP}:8500" --engine-opt="cluster-store=consul://${KV_IP}:8500"   --engine-opt="cluster-advertise=eth1:2376" queenbee
```

## **Now let's create other elements in the swarm**

```bash
docker-machine create -d digitalocean --swarm --swarm-discovery="consul://${KV_IP}:8500" --engine-opt="cluster-store=consul://${KV_IP}:8500"  --engine-opt="cluster-advertise=eth1:2376" workerbee-1
docker-machine create -d digitalocean --swarm --swarm-discovery="consul://${KV_IP}:8500" --engine-opt="cluster-store=consul://${KV_IP}:8500"  --engine-opt="cluster-advertise=eth1:2376" workerbee-2 
docker-machine create -d digitalocean --swarm --swarm-discovery="consul://${KV_IP}:8500" --engine-opt="cluster-store=consul://${KV_IP}:8500"  --engine-opt="cluster-advertise=eth1:2376" workerbee-3
```

## **Go nuts! Well, for starters, switch to the swarm master using the following command**

```bash
eval $(docker-machine env --swarm queenbee)
```
