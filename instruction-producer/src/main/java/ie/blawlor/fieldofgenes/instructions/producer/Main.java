package ie.blawlor.fieldofgenes.instructions.producer;


import com.sun.org.apache.bcel.internal.util.ClassLoader;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

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

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaHostAndPort);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        InstructionProducer instructionProducer = new InstructionProducer(new KafkaProducer<>(props));

        instructionProducer.writeFileToTopic(ClassLoader.getSystemResourceAsStream(messageFileName),
                topic, Integer.valueOf(numberOfMessages));
    }

}
