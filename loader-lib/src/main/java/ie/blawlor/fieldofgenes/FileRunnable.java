package ie.blawlor.fieldofgenes;

import java.util.List;

public abstract class FileRunnable implements Runnable {
    protected final List<Integer> files;

    public FileRunnable(List<Integer> files) {
        this.files = files;
    }
}
