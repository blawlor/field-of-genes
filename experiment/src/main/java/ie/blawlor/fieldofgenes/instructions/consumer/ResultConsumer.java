package ie.blawlor.fieldofgenes.instructions.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.ArrayList;
import java.util.List;

public class ResultConsumer {


    private final KafkaConsumer<String, String> kafkaConsumer;

    public ResultConsumer(KafkaConsumer<String, String> kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }

    public void waitforNMessages(String topic, int messagesExpected){
        List<String> topics = new ArrayList<>();
        topics.add(topic);
        kafkaConsumer.subscribe(topics);
        int messagesReceived = 0;
        while (messagesReceived < messagesExpected) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(1000);
            messagesReceived += records.count();
        }
        kafkaConsumer.unsubscribe();
    }

    public void close(){
        kafkaConsumer.close();
    }

}
