FROM ubuntu:16.10

# apt update and install global requirements
RUN apt-get clean all && \
    apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y  \
        grep            \
        sed             \
        wget            \
        openjdk-8-jre


ADD target/*-jar-with-dependencies.jar experiment.jar

ENTRYPOINT ["java", "-Xms512m", "-Xmx2048m", "-jar",  "experiment.jar"]

