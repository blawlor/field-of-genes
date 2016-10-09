FROM blawlor/blast:2.2.31-bl

# Java is already installed

ADD target/*-jar-with-dependencies.jar gccontent-benchmark.jar

ENTRYPOINT ["java", "-Xms1024m", "-Xmx6g", "-jar",  "gccontent-benchmark.jar"]

