package ie.blawlor.fieldofgenes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FileThreadRunner {
    public static void performRun(Function<List<Integer>, FileRunnable> createRunnable,
                                  int numberOfThreads,
                                  int numberOfFiles) {

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
            Thread t = new Thread(createRunnable.apply(filesList));
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
        } catch (Throwable t){
            System.out.println("Something went wrong: " + t);
            throw t;
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Total elapsed time (ms) is " + (endTime - startTime));
    }


}
