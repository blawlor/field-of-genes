package ie.blawlor.fieldofgenes.producer;

import ie.blawlor.fieldofgenes.RefSeqLoader;

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

        List<Thread> threads = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for (List<Integer> filesList: executionOrder){
            Thread t = new Thread(new DownloadRunnable(filesList));
            t.start();
            threads.add(t);
        }
        try{
            for (Thread t: threads){
                t.join();
            }
        } catch(InterruptedException e){
            //Something went wrong
            System.out.println("Thread was interrupted: " + e);
            throw new RuntimeException(e);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Total elapsed time (ms) is " + (endTime - startTime));
    }

    private static class DownloadRunnable implements Runnable {
        private final List<Integer> files;
        public DownloadRunnable(List<Integer> files) {
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
                System.out.println("Exception during download: " + ex);
                Thread t = Thread.currentThread();
                t.getUncaughtExceptionHandler().uncaughtException(t, ex);

            }
        }
    }
}
