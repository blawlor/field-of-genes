GC Content Agent
================
This project builds a Docker image to be deployed onto the Field of Genes. 
When triggered, it reads from the RefSeq topic and calculates the GC Content for each sequence, placing the results in another topic.
It uses a Java library to do the GC Content calculation.
