This document explains how to use this agent to populate the Kafka Genomic Database.

By putting job messages on particular topics, we can trigger the kgd loader agent to read database files from 
NCBI and create kafka topics with that data. The most convenient way to do this is using kafkacat:

kafkacat -P -b <broker ip> -t <topic name>
