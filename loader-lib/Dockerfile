FROM blawlor/blast:2.2.31-bl

# Java is already installed

ADD target/kafka-genomic-database-*-jar-with-dependencies.jar producer.jar

ENTRYPOINT ["java", "-Xms512m", "-Xmx2048m", "-jar",  "producer.jar"]

