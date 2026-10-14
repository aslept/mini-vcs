import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class Commit implements Serializable {
    private String parent;
    private String id;
    private String message;
    private String dateAndTime;
    private Map<String, String> files = new TreeMap<>();

    public Commit(String parent, String message, String dT) {
        this.parent = parent;
        this.message = message;
        this.dateAndTime = dT;
    }

    public String getParent() {
        return parent;
    }

    public String getMessage() {
        return message;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
