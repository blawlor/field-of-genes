package ie.lawlor.fieldofgenes.gccontent;

import ie.blawlor.fieldofgenes.gccontent.GCContent;
import org.junit.Test;

import java.io.File;
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
        File file = new File(classLoader.getResource("sample.fasta").getFile());
        List<Double> results = GCContent.calculateGC(file);
        assertEquals(1, results.size());
        double ratio = results.get(0);
        assertEquals(0.5, ratio, 0.001);

    }
}