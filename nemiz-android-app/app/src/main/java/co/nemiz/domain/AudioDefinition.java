package co.nemiz.domain;

import java.io.Serializable;
import java.util.List;

public class AudioDefinition implements Serializable {
    private int count;

    private List<Audio> files;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Audio> getFiles() {
        return files;
    }

    public void setFiles(List<Audio> files) {
        this.files = files;
    }
}
