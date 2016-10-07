Field of Genes
==============

All components required to reproduce the Field of Genes experiment are provided here.

The experiment is broken down into 2 sections: Loader and GC Content.
Each of these is divided into 2 parts: Benchmark and Experiment.

## Loader
In this section, we test the performance and scalability of Field Of Genes when
first populating it with RefSeq data. 
 
### Loader Benchmark
The Benchmark involves running a simple
multi-threaded Java program on DigitalOcean using 4, 8 and 12 threads. This program
simply downloads, unzips, untars and converts the NCBI RefSeq files.

To measure the time taken to download _f_ files using _t_ threads, run the following commands:

1. In _fabric_:
```
./create-benchmark.sh
eval $(docker-machine env benchmark)
docker run -it --rm blawlor/loader-benchmark f t
``` 
### Loader Experiment
To perform the experiment, follow these steps:
1. In _fabric_:
```
./create-swarm.sh 8
eval $(docker-machine env --swarm queenbee)
docker-compose -f docker-compose.yml -f digital_ocean.yml -p kafka up -d
docker-compose -f docker-compose.yml -f digital_ocean.yml -p kafka scale kafka=8
source set-kafka-address.sh
```

This creates 8 hosts for Kafka, then switched Docker to use those hosts for 
future Docker commands. Kafka is then deployed using Compose, and scaled so that
there is one broker for every host.
Finally a script is sourced to extract the host and port values for a Kafka broker.

2. In _loader-agent_:
```
docker-compose -f docker-compose.yml -p loader up -d
docker-compose -f docker-compose.yml -p loader scale loader=<parallelization factor (4, 8 ,12)>
```

This deploys the _loader-agent_ on the same hosts. We can scale this up to and number. The
experiment described in the paper uses three values: 4, 8 and 12. The best approach is to scale to 4 first,
then peform the experiment, then to 8 followed by another run of the experiment, and finally 12.
At each phase, capture the time taken as described below.

3. In _experiment_:
The goal is to measure the time between when the first instruction to load the data is sent, and 
when the last result is received. In the paper, we performed this using about 10% of the total 
RefSeq database. To achieve this we use a small Java application running on the local machine
but in a Docker container, which sends messages to the instructions queue, and counts the responses,
giving a final time when all expected responses have been read.

```
``` 
