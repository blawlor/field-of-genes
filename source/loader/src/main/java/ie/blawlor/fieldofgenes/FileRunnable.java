package ie.blawlor.fieldofgenes;

import java.util.List;

/**
 * A Runnable that works on a list of files 
 */
public abstract class FileRunnable implements Runnable {
    protected final List<Integer> files;

    public FileRunnable(List<Integer> files) {
        this.files = files;
    }
}
