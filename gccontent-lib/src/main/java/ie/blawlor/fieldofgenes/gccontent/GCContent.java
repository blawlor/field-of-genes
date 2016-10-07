package ie.blawlor.fieldofgenes.gccontent;

import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.DNASequence;

public class GCContent {
    public static double calculateGC(String dnaString) {
        try {
            DNASequence dnaSequence = new DNASequence(dnaString);
            return ((double)dnaSequence.getGCCount())/dnaSequence.getLength();
        } catch (CompoundNotFoundException e){
            throw new RuntimeException("Sequence was not readable as DNA");
        }
    }
}
