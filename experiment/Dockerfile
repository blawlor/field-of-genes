FROM solsson/kafka:1.0.0

ADD target/*-jar-with-dependencies.jar experiment.jar
ADD run.sh experiment.sh

ENTRYPOINT ["./experiment.sh"]

