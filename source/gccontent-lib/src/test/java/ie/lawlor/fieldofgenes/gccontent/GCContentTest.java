package ie.lawlor.fieldofgenes.gccontent;

import ie.blawlor.fieldofgenes.gccontent.GCContent;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class GCContentTest {
    @org.junit.Test
    public void calculateGC() throws Exception {
        String sample1 = "AAATTTCCCGGG";
        double result = GCContent.calculateGC(sample1);
        assertEquals(0.5, result, 0.001);
    }

    @Test
    public void calculateGCFile() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("sample.fasta");
        File file = new File(resource.getFile());
        List<Double> results = GCContent.calculateGC(file);
        assertEquals(1, results.size());
        double ratio = results.get(0);
        assertEquals(0.5, ratio, 0.001);

    }
}