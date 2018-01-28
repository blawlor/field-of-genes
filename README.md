Field of Genes
======

## Preparing Docker images
All components required to reproduce the Field of Genes experiment are provided here.

As all running software components are published as [public Docker images](https://hub.docker.com/r/blawlor/), it is not essential to compile the source code in order to run the experiments. For developers who wish to build the containers from source and use these containers for the experiment, there is a separate README under the ```source``` directory. Note that if building and using your own docker images, you will need to update the  kubernetes yaml files (described below) to substitute the ```blawlor``` image names with your own.

The rest of this README assumes that these steps are complete: that the images referenced by the kubernetes deployment files are built and published on Docker Hub.

## Running the Experiment

### Requirements
These instructions assume the following about your environment.
* **Linux**: You are running a bash shell in a Linux environment.
* **gcloud**: You have installed the [gcloud sdk](https://cloud.google.com/sdk/) - a command line utility to manage Google Cloud resources.
* **kubectl** You have installed [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/) - a command line utility to manage a Kubernetes cluster instance.

The objective is to test the hypothesis that Kafka is a scalable data repository for bioinformatic data (in this case, the RefSeq genomic database). We compare Kafka's scalability characteristics with the flat BLAST-format files that are downloadable from [NCBI](https://www.ncbi.nlm.nih.gov/refseq/). We use a [GC Content](https://en.wikipedia.org/wiki/GC-content) calculation as a placeholder algorithm to provide the comparison, but any per-sequence processing algorithm could be substituted. When we talk about 'processing' in this text, we are referring to GC Content.

### Kubernetes on Google Cloud
All code is run on the cloud, using [Kubernetes(k8s)](https://kubernetes.io/)-orchestrated [Docker](https://www.docker.com/) containers. We have used [Google Kubernetes Engine](https://cloud.google.com/kubernetes-engine/) (GKE) as our platform and you will need a Google Cloud account to run this experiment. Note that this is a *paid platform*. You will be charged by Google for resources used, so make sure to destroy your k8s cluser *and any extra SSDs* that are created using your account, when you are finished an experimental run. For cluster sizes of 8 and larger, Google may ask you to increase your [quotas](https://cloud.google.com/compute/quotas).

Although the provided instructions for creating the k8s cluster are specific to GKE (i.e they use [gcloud](https://cloud.google.com/sdk/gcloud/), most of the k8s deployment instructions will work on k8s clusters hosted elsewhere (e.g. Azure, or on a private k8s cluster if you have access to one). The only known exceptions are the storage configuration yml files mentioned in the instructions below, would would need to be substituted with platform-specific alternatives.

In order for the gcloud scripts (described in more detail below) to work, you will need to have created a Google cloud project and.....[finish this]

Alternatively, [contact the author](mailto:brendan.lawlor@gmail.com) to arrange the temporary use of his account if you are reviewing this experiment for a publication.

### Experiment Design

The experiment is broken down into 2 sections: Benchmark and Experiment. The Benchmark measures the speed at which we can process increasing amounts of RefSeq fasta-format data, on a single node (flat files residing on a linux file system are, by their nature, not distributed). The Experiment measures the processing of these same sequences from a Kafka topic, on Kafka cluster sizes of 4, 8 and 12 nodes, using [Akka](https://akka.io/) actors. The Akka actors are simply vehicles for invoking the same GC Content code as the Benchmark, but in the parallel, streamed and distributed manner that the Kafka topic facilites. We chose Akka because this is a technology we are familiar with from other research.

Benchmark and Experiment are both broken into two phases: Loader and GCContent. 

* The loader phase prepares the data. In the case of Benchmark, this involves FTPing tarred and zipped files from NCBI, and then untarring, unzipping and running the ```blastdbcmd``` [utility](https://www.ncbi.nlm.nih.gov/books/NBK279689/) to extract fasta files from the BLAST format. In the case of the Experiment, the loader must do the same as the Benchmark and then publish the sequences into a Kafka topic (called ```refseq```).
* The gccontent phase is where the GC Content algorithm (our own Java-based implementation) is run on every refseq sequence.

The experimental runs are parameterized along two dimensions:

* Number of files (~ number of sequences). This is effectively the 'size' of the experiment. Each new file downloaded from NCBI increases the number of sequences to be processed.
* Parallelization Factor. In the case of the Benchmark code, this is the number of independent threads we create to perform the loading and gc content processing. In the case of the Experiment, this corresponds to the number of Akka actors created to do the loading/gc content processing. We also use this factor to decide how many partitions to create for each topic.


### Benchmark
The gccontent benchmark is run by launching the gccontent-benchmark Docker image as a kubernetes Pod. The Docker image, when run, simply invokes the gccontent executable jar which first downloads and expands the stipulated number of files from NCBI, and then measures the time taken to run the gccontent algorithm over those files, using the stipulated number of threads. The ```run-benchmark-gccontent.sh``` file manages the entire process. The results will be tracked and displayed on the bash shell. For example, to run the benchmark using 4 files and 4 threads:

	./run-benchmark-gccontent.sh 4 4

This creates a single-worker-node Kubernetes cluster and launches the gccontent-benchmark Docker image on it. We typically run this with value 4/4, 8/8, 12/12 and 16/16.

### Experiment
Running the experiment is more complex and can be viewed as two phases:

#### Prepare the Kafka cluster
##### Overview
We must create a multi-node kubernetes cluster (4,8 or 12 nodes) and then bring up a multi-node Kafka cluster including its accompanying Zookeeper instances. To do this, we have leaned heavily on the work done by [yolean](https://github.com/Yolean/kubernetes-kafka).
##### Detailed steps:
1. Run the ```create-k8s-cluster.sh``` script, passing in the cluster size. E.g. 
```
./create-k8s-cluster.sh 4

```
This will first download the required number of files and then run the gccontent code on those files.

#### Run the experiment
##### Overview
For each configuration of node size and number of files/sequences/parallelization factor, we must create the specified topics with the correct number of partitions, launch loader agents that can populate the Kafka ```refseq``` topic and then launch gc-content agents capable of running the gc-content algorithm in a parallel and coordinated way on the Kafka refseq topic, putting the results into the ```refseq-gccontent``` topic.
In order to measure the run times of the experiment, we need a mechanism for triggering them at the same time, and detecting when processing is complete. In a distributed system like this, it is not trivial. What follows is an overview of how we achieve this:

1. Launch the stipulated number of loader agents. They do nothing until they read an instruction from the ```loader``` Kafka topic (i.e. we use Kafka itself not only as a source of the data, but also as a message channel for managing the experiment). Wait until all agents are up and running on Kubernetes before proceeding to next step.
2. Launch the loader experiment. This sends the instructions to the ```loader``` topic and then enters a timed loop, listening to the ```refseq``` topic until it stops increasing (i.e. until the loading is complete).
3. Launch the stipulated number of gccontent-agents. The do nothing until the read an instruction from the ```gccontent``` topic. Wait until all agents are up and running before proceding to the next step.
4. Launch the gccontent experiment. This sends the instructions to the ```gccontent``` topic and then enters a timed loop, listening to the ```refseq-gccontent``` topic until it stops increasing (i.e. until the processing is complete).

##### Detailed Steps
1. Create the topics, specifying the parallelization factor (i.e. the number of partitions). E.g for a parallelization factor of 4:
```
cd topics
./create-topics.sh 4
cd ..
```
2. Launch the required number of loader agents. E.g. to launch 4:
```
./deploy-loader-agents.sh 4
```
3. Run the loader experiment, specifying the parallelization factor and monitor the logs. E.g for parallelization factor 4:
```
cd loader-experiment
./loader-experiment.sh 4
cd ..
```
4. Launch the required number of gccontent agents: E.g. to launch 4:
```
./deploy-gccontent-agents.sh 4
```
5. Run the gccontent experiment, specifying the parallelization factor and monitor the logs. E.g for parallelization factor 4:
```
cd gccontent-experiment
./gccontent-experiment.sh 4
cd ..
```