FROM blawlor/blast:2.2.31-bl

# Java is already installed
# Dockerfile for the blawlor/loader-benchmark image.

ADD target/*-jar-with-dependencies.jar loader-benchmark.jar

ENTRYPOINT ["java", "-Xms1024m", "-Xmx4g", "-jar",  "loader-benchmark.jar"]

