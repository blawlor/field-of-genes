package ie.blawlor.fieldofgenes.gccontent;

import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GCContent {
    public static double calculateGC(String dnaString) {
        try {
            DNASequence dnaSequence = new DNASequence(dnaString);
            return ((double) dnaSequence.getGCCount()) / dnaSequence.getLength();
        } catch (CompoundNotFoundException e) {
            throw new RuntimeException("Sequence was not readable as DNA");
        }
    }

    public static List<Double> calculateGC(File fastaFile) {
        List<Double> results = new ArrayList<>();
        try {
            Map<String, DNASequence> sequenceMap = FastaReaderHelper.readFastaDNASequence(fastaFile);
            for (DNASequence dnaSequence: sequenceMap.values()){
                results.add(((double) dnaSequence.getGCCount()) / dnaSequence.getLength());
            }
            return results;
        } catch (IOException e) {
            e.printStackTrace();
            return  results;
        }
    }
}