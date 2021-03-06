package ie.blawlor.fieldofgenes.gccontent;

import ie.blawlor.fieldofgenes.DownloadRunnable;
import ie.blawlor.fieldofgenes.FileThreadRunner;
import ie.blawlor.fieldofgenes.FileRunnable;
import ie.blawlor.fieldofgenes.RefSeqLoader;
import ie.blawlor.fieldofgenes.producer.RefSeqProducer;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int numberOfThreads = 1;
        int numberOfFiles = Integer.valueOf(args[0]);
        if (args.length > 1) {
            numberOfThreads = Integer.valueOf(args[1]);
        }
        // First, download the number of files required.
        FileThreadRunner.performRun((list) -> new DownloadRunnable(list),
                numberOfThreads,
                numberOfFiles);
        System.out.println("About to run GCContent on the downloaded files");
        FileThreadRunner.performRun((list) -> new GCContentRunnable(list),
                numberOfThreads,
                numberOfFiles);
    }

    private static class GCContentRunnable extends FileRunnable {
        public GCContentRunnable(List<Integer> files) {
            super(files);
            System.out.println("Creating new thread with " + files.size() + " files");
        }

        private String createRootName(Integer index){
            return RefSeqProducer.generateSourceFileName("refseq_genomic.NNN", index);
        }

        @Override
        public void run() {
            try {
                for (Integer e : files) {
                    File genrefFile = new File(RefSeqProducer.DATABASES_ROOT_DIR + "/" +createRootName(e)+".fasta");
                    System.out.println("About to calculate the GC Content for file " + genrefFile);
                    List<Double> ratios = GCContent.calculateGC(genrefFile);
                    FileWriter writer = new FileWriter(RefSeqProducer.DATABASES_ROOT_DIR + "/" +createRootName(e)+".gc");
                    for(Double ratio: ratios) {
                        writer.write(ratio.toString());
                    }
                    writer.close();
                }
            } catch (Throwable ex) {
                System.out.println("Exception during download: " + ex);
                Thread t = Thread.currentThread();
                t.getUncaughtExceptionHandler().uncaughtException(t, ex);

            }
        }
    }
}
