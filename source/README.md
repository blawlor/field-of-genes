Source
======

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

## 

