FROM blawlor/blast:2.2.31-bl
MAINTAINER Brendan Lawlor "brendan.lawlor@gmail.com"

ADD target/scala-*/*-assembly-*.jar gccontent-agent.jar
ADD startup.sh startup.sh
RUN mkdir /data/log
VOLUME /data/log
ENTRYPOINT ["./startup.sh" ]
