package ie.blawlor.kgd.producer;

import net.sf.jfasta.FASTAElement;
import net.sf.jfasta.impl.FASTAElementImpl;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.mockito.Mockito.*;

import static org.junit.Assert.assertEquals;

public class RefSeqProducerTest {

    KafkaProducer<String, String> kafkaProducer = mock(KafkaProducer.class);

    @org.junit.Before
    public void setUp() throws Exception {

    }

    @org.junit.Test
    public void breakDownSequenceShortString() throws Exception {
        String seq = "ATAGTAGCGCTGTAGATCGGATAGATCGGGCGCGTCGCTGAGCTGCTGTVTGCT";
        FASTAElement fastaElement = new FASTAElementImpl("gi|123", seq);
        List<RefSeqProducer.KeySequence> result = RefSeqProducer.breakDownSequence(fastaElement);
        assertEquals(1, result.size());
        assertEquals(seq, result.get(0).getSequence());
        assertEquals("gi|123-0", result.get(0).getKey());
    }

    @org.junit.Test
    public void breakDownSequenceTenThousand() throws Exception {
        String seq = generateSequence(100000);
        FASTAElement fastaElement = new FASTAElementImpl("gi|123", seq);
        List<RefSeqProducer.KeySequence> result = RefSeqProducer.breakDownSequence(fastaElement);
        assertEquals(1, result.size());
        assertEquals(seq, result.get(0).getSequence());
        assertEquals("gi|123-0", result.get(0).getKey());
    }

    @org.junit.Test
    public void breakDownSequenceTenThousandAndOne() throws Exception {
        String seq = generateSequence(100001);
        FASTAElement fastaElement = new FASTAElementImpl("gi|123", seq);
        List<RefSeqProducer.KeySequence> result = RefSeqProducer.breakDownSequence(fastaElement);
        assertEquals(2, result.size());
        assertEquals(seq.substring(0,100000), result.get(0).getSequence());
        assertEquals("gi|123-0", result.get(0).getKey());
        assertEquals(seq.substring(100000,100001), result.get(1).getSequence());
        assertEquals("gi|123-1", result.get(1).getKey());
    }

    private static String generateSequence(int length){
        return Stream.generate(() -> String.valueOf('A')).limit(length).collect(Collectors.joining());
    }
}