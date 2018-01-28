FROM solsson/kafka:1.0.0

# Dockerfile for the blawlor/experiment image.

ADD target/*-jar-with-dependencies.jar experiment.jar
ADD run.sh experiment.sh

ENTRYPOINT ["./experiment.sh"]

