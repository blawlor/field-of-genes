Instruction Producer
===================

This is a simple java commandline appliction that takes two parameters: a file of messages, and a topic name.
It sends each line in the file as a message to the named topic, applying a key starting from 0 and incrementing 
in steps of 1.

The code can either be invoked as an executable jar, or by using the public Docker image that is created from this code. 
