package com.practise;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Repo {
    /** init- creates the repo folder and necessary folders and creates an empty commit */
    public static void init() {
        Path repo = Path.of(".mini-vcs");
        Path commits = Path.of(".mini-vcs/commits");
        Path blobs = Path.of(".mini-vcs/blob");
        Path tempDir = Path.of(".mini-vcs/tempDir");
        Path tempFile = Path.of(".mini-vcs/tempDir/tempFile.txt");

        if (Files.exists(repo)) {
            System.out.println("Repository already exists!\n");
            return;
        }

        try {
            Files.createDirectory(repo);
            Files.createDirectory(commits);
            Files.createDirectory(blobs);
            Files.createDirectory(tempDir);

            StagingArea stagingArea = new StagingArea();
            Commit initialCommit = new Commit(null, "Initialization Commit", "Date");

            Helper.objectWrite(initialCommit, tempFile.toString());
            String hex = Helper.sha1(tempFile);
            String path = ".mini-vcs/" + hex;
            Helper.objectWrite(initialCommit, path);

            stagingArea.setCurr(hex);
            Helper.objectWrite(stagingArea, ".mini-vcs/stagingArea.txt");

            Files.delete(tempFile);

            System.out.println("Repository successfully initialized!\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** status- it has the following jobs,
     * 1) checks the entries in the current commit and see if the map in the staging area has it.
     * 2) if it does, checks the sha1 hash of the file in directory against the one in the staging area's map.
     * 3) if they are different, mark the file as modified, if the file does not exist, mark the file as "deleted"
     * 4) if the file is not in the staging area, compare the sha1 of the file in the directory against
     * the one in the commit and if it is changed, mark it as modified.
     * 5) if it is deleted, mark the file as "deleted"
     * 6) show untracked file. */
    public static void status() {
        StagingArea star = (StagingArea) Helper.objectRead(".mini-vcs/stagingArea.txt");
        String path = ".mini-vcs/commit/" + star.getCurr();
        Commit curr = (Commit) Helper.objectRead(path);

        Map<String, String> starFiles = star.getFiles();
        Map<String,String> commitFiles = curr.getFiles();

        List<String> modified = new ArrayList<>();
        List<String> deleted = new ArrayList<>();

        for (Map.Entry<String, String> file : commitFiles.entrySet()) {
            String fileLocation = file.getKey();

            if (starFiles.containsKey(fileLocation)) {
                String hexValue = starFiles.get(fileLocation);

                if (Files.exists(Path.of(fileLocation))) {
                    String fileHex = Helper.sha1(Path.of(fileLocation));
                    if (!fileHex.equalsIgnoreCase(hexValue)) { modified.add(fileLocation); }
                } else {
                    deleted.add(fileLocation);
                }
            } else {
                String hexValue = file.getValue();

                if (Files.exists(Path.of(fileLocation))) {
                    String fileHex = Helper.sha1(Path.of(fileLocation));
                    if (!fileHex.equalsIgnoreCase(hexValue)) { modified.add(fileLocation); }
                } else {
                    deleted.add(fileLocation);
                }
            }
        }

        System.out.println("To be commited: \n");
        for (Map.Entry<String, String> file : starFiles.entrySet()) {
            if (modified.contains(file.getKey()) || deleted.contains(file.getKey())) { continue; }

            System.out.println(file.getKey() + "\n");
        }

        System.out.println("Modifications not staged: \n");
        for (String s : modified) {
            System.out.println(s + "\n");
        }

        System.out.println("Deleted: \n");
        for (String d : modified) {
            System.out.println(d + "\n");
        }
    }

    /** add- it receives the file location as the input.
     * it has the following jobs,
     * 1) */
    public static void add(String s) {

    }
}

class StagingArea implements Serializable {
    private String currName;
    private Map<String, String> files;

    public void setCurr(String curr) {
        this.currName = curr;
    }

    public String getCurr() {
        return currName;
    }

    public Map<String, String> getFiles() {
        return files;
    }
}
