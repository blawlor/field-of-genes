package ie.blawlor.kgd;

import ie.blawlor.kgd.producer.RefSeqProducer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class RefSeqLoader {

    public static String load(String instruction, String topic, String kafkaHostAndPort){

        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaHostAndPort);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());

        RefSeqProducer refSeqProducer = new RefSeqProducer(instruction, new KafkaProducer<>(props));
        long started = System.currentTimeMillis();
        try {
            refSeqProducer.loadDB(topic);
            long ended = System.currentTimeMillis();
            long elapsed = ended - started;
            return successMessage(refSeqProducer.getId(), elapsed);
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"id\":" + refSeqProducer.getId() + "\"result\": \"Error: " + e.getMessage() + "\"}";
        } finally {
            refSeqProducer.close();
        }
    }

    public static String successMessage(String id, long elapsed){
        return "{\"id\":" + id +
                "\"result\": \"Success\"" +
                "\"finished\": \""+ LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +"\"" +
                "\"elapsed\": "+ elapsed +
                "}";
    }



    public static String recreateDBDescription(String id, String range){
        return "{\n" +
                "  \"topics\": [\n" +
                "    {\n" +
                "      \"id\" : \""+id+"\",\n" +
                "      \"url-root\": \"ftp://ftp.ncbi.nlm.nih.gov/blast/db/\",\n" +
                "      \"file-template\": \"refseq_genomic.NNN.tar.gz\",\n" +
                "      \"entry-range\": \""+range+"\",\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
}
