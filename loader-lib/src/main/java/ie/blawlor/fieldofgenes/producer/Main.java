package ie.blawlor.fieldofgenes.producer;

import ie.blawlor.fieldofgenes.DownloadRunnable;
import ie.blawlor.fieldofgenes.FileThreadRunner;

public class Main {

    public static void main(String[] args) throws Exception {
        int numberOfThreads = 1;
        int numberOfFiles = Integer.valueOf(args[0]);
        if (args.length > 1) {
            numberOfThreads = Integer.valueOf(args[1]);
        }
        FileThreadRunner.performRun((list) -> new DownloadRunnable(list),
                numberOfThreads,
                numberOfFiles);
    }
}



