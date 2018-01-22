Source
======

## loader
This is a Java maven project that produces a library jar file with the code to load files from NCBI using FTP. (to be used by the loader-agent project), an executable jar file with all its depnedencies for running the benchmark gccontent and a Dockerfile to encapsulate the executable jar.

### Build Requirements
* JDK 8
* Maven 3
* Docker

### Build Instructions

	mvn clean install
    docker build -t blawlor/loader-benchmark

Note: If you plan to push this image to your own public docker image registry, replace the 'blawlor' with your own registry name.


## loader-agent
This is a Scala Akka project that produces an executable jar with a system of actors. Based on its configuration, it listens to a kafka _instruction_ topic for instructions, and on receiving them, runs the loader code from the ```loader``` project to download and expand genref files and load their contents into the ```refseq``` topic on Kafka.

### Build Requirements
* JDK 8
* SBT
* Docker

### Build Instructions

```
sbt clean assembly
```


## gccontent
This is a Java maven project that produces a library jar file with the gccontent algorithm (to be used by the gccontent-agent project), an executable jar file with all its depnedencies for running the benchmark gccontent and a Dockerfile to encapsulate the executable jar. 

### Build Requirements
* JDK 8
* Maven 3
* Docker 

### Build Instructions

	mvn clean install 
    docker build -t blawlor/gccontent-benchmark

Note: If you plan to push this image to your own public docker image registry, replace the 'blawlor' with your own registry name.

### Build output
* gccontent-1.0-SNAPSHOT.jar
* gccontent-1.0-SNAPSHOT-jar-with-dependencies.jar
* blawlor/gccontent-benchmark (docker image)

## gccontent-agent
This is a Scala Akka project that produces an executable jar with a system of actors. Based on its configuration, it listens to a kafka _instruction_ topic for instructions, and on receiving them, runs the GCContent library from the ```gccontent``` project on every element in an input topic. It puts the resulting GCContent ratio for every sequence it reads into an output topic.

### Build Requirements
* JDK 8
* SBT
* Docker

### Build Instructions

```
sbt clean assembly
```

