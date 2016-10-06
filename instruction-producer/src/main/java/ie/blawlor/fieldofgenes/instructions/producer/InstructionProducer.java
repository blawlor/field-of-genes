package ie.blawlor.fieldofgenes.instructions.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.*;

public class InstructionProducer {


    private final KafkaProducer<String, String> kafkaProducer;

    public InstructionProducer(KafkaProducer<String, String> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public void close(){
        kafkaProducer.close();
    }


    public void writeFileToTopic(InputStream messages, String topic, int numberOfLines) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(messages));
        try {
            String line = br.readLine();
            int key = 0;
            int linesRead = 0;
            while (line != null && linesRead < numberOfLines) {
                ProducerRecord<String, String> producerRecord =
                        new ProducerRecord<>(topic,
                                "" + key,
                                line);
//                kafkaProducer.send(producerRecord);
                System.out.println(producerRecord.toString());
                key++;
                linesRead++;
                line = br.readLine();
            }
        } finally {
            br.close();
            close();
        }

    }
}
