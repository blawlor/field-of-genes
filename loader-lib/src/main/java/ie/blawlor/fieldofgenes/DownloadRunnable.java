package ie.blawlor.fieldofgenes;

import java.util.List;

public class DownloadRunnable extends FileRunnable {
    public DownloadRunnable(List<Integer> files) {
        super(files);
        System.out.println("Creating new thread with " + files.size() + " files");
    }

    @Override
    public void run() {
        try {
            for (Integer e : files) {
                String instruction = RefSeqLoader.recreateDBDescription("" + e, "" + e);
                System.out.println(instruction);
                RefSeqLoader.download(instruction);
            }
        } catch (Throwable ex) {
            System.out.println("Exception during download: " + ex);
            Thread t = Thread.currentThread();
            t.getUncaughtExceptionHandler().uncaughtException(t, ex);

        }
    }
}