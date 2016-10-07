package ie.lawlor.fieldofgenes.gccontent;

import ie.blawlor.fieldofgenes.gccontent.GCContent;

import static org.junit.Assert.*;

public class GCContentTest {
    @org.junit.Test
    public void calculateGC() throws Exception {
        String sample1 = "AAATTTCCCGGG";
        double result = GCContent.calculateGC(sample1);
        assertEquals(0.5, result, 0.001);
    }

}