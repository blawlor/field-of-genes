package ie.blawlor.fieldofgenes.gccontent;

import ie.blawlor.fieldofgenes.producer.RefSeqProducer;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.compound.AmbiguityDNACompoundSet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GCContent {
    public static double calculateGC(String dnaString) {
        return calculateGC(dnaString, "Unspecified");
    }

    public static double calculateGC(String dnaString, String filename) {
        try {
            DNASequence dnaSequence = new DNASequence(dnaString, AmbiguityDNACompoundSet.getDNACompoundSet());
            return ((double) dnaSequence.getGCCount()) / dnaSequence.getLength();
        } catch (CompoundNotFoundException e) {
            System.out.println("Error when processing : "+ filename );
            e.printStackTrace();
            throw new RuntimeException("Sequence was not readable as DNA", e);
        }
    }

    public static List<Double> calculateGC(File fastaFile) {
        List<Double> results = new ArrayList<>();
        try {

            FileInputStream fis = new FileInputStream(fastaFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            boolean fileNotComplete = true;
            String keyRoot = "";
            while(fileNotComplete) {
                if (line != null && line.startsWith(">")) {
                    keyRoot = line;
                    line = br.readLine();
                } else if (!keyRoot.isEmpty()) {
                    boolean sequenceNotComplete = true;
                    int subsequenceNumber = 0;
                    while (sequenceNotComplete) {
                        // Iterate here until we end the file or find a new line with key
                        StringBuilder currentSequence = new StringBuilder();
                        while (line != null &&
                                !line.startsWith(">") &&
                                currentSequence.length() < (RefSeqProducer.MAX_RECORD_SIZE - line.length())) {
                            currentSequence.append(line);
                            line = br.readLine();
                        }
                        if (line == null) {
                            sequenceNotComplete = false;
                            fileNotComplete = false;
                        } else if (line.startsWith(">")) {
                            sequenceNotComplete = false;
                            fileNotComplete = true;
                        } else { //Sequence longer than max.
                            sequenceNotComplete = true;
                            fileNotComplete = true;
                            subsequenceNumber++;
                            // Don't want to lose last line
                            currentSequence.append(line);
                        }
                        results.add(calculateGC(currentSequence.toString(), fastaFile.getName()));
                    }
                } else if (line == null){
                    fileNotComplete = false;
                } else {
                    //Keep going to next key (at start)
                    line = br.readLine();
                }
            }

            br.close();
//            fastaFile.delete();

            return results;
        } catch (IOException e) {
            e.printStackTrace();
            return  results;
        }
    }

}