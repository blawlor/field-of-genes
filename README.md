Field of Genes
==============

All components required to reproduce the Field of Genes experiment are provided here.

The experiment is broken down into 2 sections: Benchmark and Experiment.
The Benchmark is further broken down into Loader and GC Content, but the Experiment performs both Loader and GC Content together.

## Benchmark
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
### GC Content Benchmark

1. In _gccontent-lib_::
```
docker run -it --rm blawlor/gccontent-benchmark f t
```
This will first download the required number of files and then run the gccontent code on those files.

## Experiment

### Loader and GC Experiment
To perform the experiment, follow these steps:
1. In _fabric_:
```
./create-swarm.sh 8
eval $(docker-machine env --swarm queenbee)
```
2. In base directory:
```
./run.sh 8 4
```
The two steps above are for a node of size 8, and a parallelization factor of 4.
For bigger clusters and higher parallelization factors, just replace the values of 12 and 4 above as appropriate.

3. In base directory:
```
./clean.sh
```
This removes any agents that are running, brings the kafka cluster down, clears any dangling volumes on the docker swarm but leaves the hosts intact. 

4. In _fabric_
```
./destroy.sh
```

Do this at the end of all experiments in order to remove all hosts from Digital Ocean.
