package com.practise;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class Commit implements Serializable {
    private Commit parent;
    private String message;
    private String dateAndTime;
    private Map<String, String> files = new TreeMap<>();

    public Commit(Commit p, String m, String dT) {
        this.parent = p;
        this.message = m;
        this.dateAndTime = dT;
    }

    public Commit getParent() {
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
}
