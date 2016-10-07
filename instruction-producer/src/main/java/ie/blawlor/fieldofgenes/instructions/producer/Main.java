package ie.blawlor.fieldofgenes.instructions.producer;


import com.sun.org.apache.bcel.internal.util.ClassLoader;
import ie.blawlor.fieldofgenes.instructions.consumer.ResultConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.SystemTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {

    /**
     * Takes parameters to specify the host and port of kafka as well as the topic to
     * which it should send messages. It should also take the name of the message file
     * and the number of messages to send.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String kafkaHostAndPort = args[0];
        String topic = args[1];
        String messageFileName = args[2];
        String numberOfMessages = args[3];

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaHostAndPort);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        InstructionProducer instructionProducer = new InstructionProducer(new KafkaProducer<>(producerProps));

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHostAndPort);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "loader-results");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
        ResultConsumer resultConsumer = new ResultConsumer(new KafkaConsumer<>(consumerProps));

        System.out.println("Sending instructions to Kafka on " + kafkaHostAndPort);
        long startTime = System.currentTimeMillis();
        System.out.println("Start time: " + startTime);
        instructionProducer.writeFileToTopic(
                ClassLoader.getSystemResourceAsStream(messageFileName),
                topic,
                Integer.valueOf(numberOfMessages));

        // Listen to the result queue until the 'numberOfMessages' is received.
        System.out.println("Listening to " + topic+"-res" + " for responses.");
        resultConsumer.waitforNMessages(topic+"-res", Integer.valueOf(numberOfMessages));
        long stopTime = System.currentTimeMillis();
        System.out.println("Stop time: " + stopTime);
        System.out.println("Elapsed time: " + (stopTime - startTime));
        resultConsumer.close();
    }

}
