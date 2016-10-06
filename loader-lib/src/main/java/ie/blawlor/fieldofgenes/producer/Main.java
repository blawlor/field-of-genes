package ie.blawlor.fieldofgenes.producer;

import ie.blawlor.fieldofgenes.RefSeqLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        int numberOfThreads = 1;
        int numberOfFiles = Integer.valueOf(args[0]);
        if (args.length > 1) {
            numberOfThreads = Integer.valueOf(args[1]);
        }
        int filesPerThread = numberOfFiles/numberOfThreads;
        int remainingFiles = numberOfFiles%numberOfThreads;
        List<List<Integer>> executionOrder = new ArrayList<>();
        int entryNumber = 0;
        for (int t = 0; t < numberOfThreads; t++) {
            List<Integer> threadEntryNumbers = new ArrayList<>();
            int numberOfLoops = filesPerThread;
            if (t < remainingFiles) {
                numberOfLoops += 1;
            }
            for(int i = 0; i < numberOfLoops; i++) {
                threadEntryNumbers.add(entryNumber);
                entryNumber++;
            }
            executionOrder.add(threadEntryNumbers);
        }

        for (List<Integer> thread: executionOrder){
            DownloadThread t = new DownloadThread(thread);
            (new Thread(t)).start();
        }
    }

    private static class DownloadThread implements Runnable {
        private final List<Integer> files;
        public DownloadThread(List<Integer> files) {
            System.out.println("Creating new thread with " + files.size() + " files");
            this.files = files;
        }

        @Override
        public void run() {
            try {
                for (Integer e : files) {
                    String instruction = RefSeqLoader.recreateDBDescription("" + e, "" + e);
                    System.out.println(instruction);
                    RefSeqLoader.download(instruction);
                }
            } catch (Exception ex) {
                Thread t = Thread.currentThread();
                t.getUncaughtExceptionHandler().uncaughtException(t, ex);
            }
        }
    }
}
