Field of Genes
======

## Preparing Docker images
All components required to reproduce the Field of Genes experiment are provided here.

As all running software components are published as [public Docker images](https://hub.docker.com/r/blawlor/), it is not essential to compile the source code in order to run the experiments. For developers who wish to build the containers from source and use these containers for the experiment, there is a separate README under the ```source``` directory. Note that if using your own docker images, you will need to update kubernetes yaml files to substitute the blawlor image names with your own.

The rest of this README assumes that these steps are complete: that the images referenced by the kubernetes deployment files are built and published on Docker Hub.

## Running the Experiment

The objective is to test the hypothesis that Kafka is a scalable data repository for bioinformatic data (in this case, the RefSeq genomic database). We compare Kafka's scalability characteristics with the flat BLAST-format files that are downloadable from [NCBI](https://www.ncbi.nlm.nih.gov/refseq/). We use a [GC Content](https://en.wikipedia.org/wiki/GC-content) calculation as a placeholder algorithm to provide the comparison, but any per-sequence processing algorithm could be substituted. When we talk about 'processing' in this text, we are referring to GC Content.

###Kubernetes on Google Cloud
All code is run on the cloud, using [Kubernetes(k8s)](https://kubernetes.io/)-orchestrated [Docker](https://www.docker.com/) containers. We have used [Google Kubernetes Engine](https://cloud.google.com/kubernetes-engine/) (GKE) as our platform and you will need a Google Cloud account to run this experiment. Note that this is a *paid platform*. You will be charged by Google for resources used, so make sure to destroy your k8s cluser *and any extra SSDs* that are created using your account, when you are finished an experimental run. For cluster sizes of 8 and larger, Google may ask you to increase your [quotas](https://cloud.google.com/compute/quotas).

Although the provided instructions for creating the k8s cluster are specific to GKE (i.e they use [gcloud](https://cloud.google.com/sdk/gcloud/), most of the k8s deployment instructions will work on k8s clusters hosted elsewhere (e.g. Azure, or on a private k8s cluster if you have access to one). The only known exceptions are the storage configuration yml files mentioned in the instructions below, would would need to be substituted with platform-specific alternatives.

Alternatively, [contact the author](mailto:brendan.lawlor@gmail.com) to arrange the temporary use of his account if you are reviewing this experiment for a publication.

###Experiment Design

The experiment is broken down into 2 sections: Benchmark and Experiment. The Benchmark measures the speed at which we can process increasing amounts of RefSeq fasta-format data, on a single node (flat files residing on a linux file system are, by their nature, not distributed). The Experiment measures the processing of these same sequences from a Kafka topic, on Kafka cluster sizes of 4, 8 and 12 nodes, using [Akka](https://akka.io/) actors. The Akka actors are simply vehicles for invoking the same GC Content code as the Benchmark, but in the parallel, streamed and distributed manner that the Kafka topic facilites. We chose Akka because this is a technology we are familiar with from other research.

Benchmark and Experiment are both broken into two phases: Loader and GCContent. 

* The loader phase prepares the data. In the case of Benchmark, this involves FTPing tarred and zipped files from NCBI, and then untarring, unzipping and running the ```blastdbcmd``` [utility](https://www.ncbi.nlm.nih.gov/books/NBK279689/) to extract fasta files from the BLAST format. In the case of the Experiment, the loader must do the same as the Benchmark and then publish the sequences into a Kafka topic (called ```refseq```).
* The gccontent phase is where the GC Content algorithm (our own Java-based implementation) is run on every refseq sequence.

The experimental runs are parameterized along two dimensions:

* Number of files (~ number of sequences). This is effectively the 'size' of the experiment. Each new file downloaded from NCBI increases the number of sequences to be processed.
* Parallelization Factor. In the case of the Benchmark code, this is the number of independent threads we create to perform the loading and gc content processing. In the case of the Experiment, this corresponds to the number of Akka actors created to do the loading/gc content processing. We also use this factor to decide how many partitions to create for each topic.



### Benchmark

 
#### Loader Benchmark
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

1. In _gccontent-lib_, _after having run the loader benchmark_:
```
docker run -it --rm blawlor/gccontent-experiment f t
```

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
