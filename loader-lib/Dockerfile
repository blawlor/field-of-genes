FROM blawlor/blast:2.2.31-bl

# Java is already installed

ADD target/*-jar-with-dependencies.jar experiment.jar

ENTRYPOINT ["java", "-Xms512m", "-Xmx2048m", "-jar",  "experiment.jar"]

