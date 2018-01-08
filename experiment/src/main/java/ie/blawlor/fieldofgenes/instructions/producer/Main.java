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
        String numberOfMessage = args[3];
        String resultTopic = null;
        String numberOfSeconds = null;
        if (args.length > 4) {
            resultTopic = args[4];
        }
        if (args.length > 5) {
            numberOfSeconds = args[5];
        }
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaHostAndPort);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        InstructionProducer instructionProducer = new InstructionProducer(new KafkaProducer<>(producerProps));

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHostAndPort);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "experiment");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");


        System.out.println("Sending instructions to Kafka on " + kafkaHostAndPort);
        long startTime = System.currentTimeMillis();
        System.out.println("Start time: " + startTime);
        instructionProducer.writeFileToTopic(
                ClassLoader.getSystemResourceAsStream(messageFileName),
                topic,
                Integer.valueOf(numberOfMessage));
        //Create a consumer in order to create its group. Will use this group in the experiment scripts to check
        // topic growth in order to understand when the experiment is finished.
        ResultConsumer resultConsumer = new ResultConsumer(new KafkaConsumer<>(consumerProps));
        resultConsumer.waitforNMessages(resultTopic, 1); // Wait for just one message - enough to create the group
        System.out.println("One message received on " + resultTopic);

//        if (resultTopic != null && !resultTopic.equalsIgnoreCase("ignore")) {
//            ResultConsumer resultConsumer = new ResultConsumer(new KafkaConsumer<>(consumerProps));
//
//            if (resultTopic == null) {
//                System.out.println("Listening to " + topic + "-res" + " for " + numberOfMessage + " responses.");
//                resultConsumer.waitforNMessages(topic + "-res", Integer.valueOf(numberOfMessage));
//            } else {
//                System.out.println("Listening to " + resultTopic + " for " + numberOfSeconds + " seconds.");
//                resultConsumer.waitUntilNSeconds(resultTopic, Integer.valueOf(numberOfSeconds));
//            }
//            long stopTime = System.currentTimeMillis();
//            if (resultTopic != null) {
//                stopTime = stopTime - (Integer.valueOf(numberOfSeconds) * 1000);
//            }
//            System.out.println("Stop time: " + stopTime);
//            System.out.println("Elapsed time: " + (stopTime - startTime));
//            resultConsumer.close();
//        }
    }
}