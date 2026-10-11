package com.practise;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Repo {
    private static boolean isRepo() {
        return Files.exists(Path.of(".mini-vcs"));
    }

    /** init- creates the repo folder and necessary folders and creates an empty commit */
    public static void init() {
        Path repo = Path.of(".mini-vcs");
        Path commits = Path.of(".mini-vcs/commits");
        Path blobs = Path.of(".mini-vcs/blob");
        Path tempDir = Path.of(".mini-vcs/tempDir");
        Path tempFile = Path.of(".mini-vcs/tempDir/tempFile.txt");

        if (Files.exists(repo)) {
            System.out.println("Repository already exists!");
            return;
        }

        try {
            Files.createDirectory(repo);
            Files.createDirectory(commits);
            Files.createDirectory(blobs);
            Files.createDirectory(tempDir);

            StagingArea stagingArea = new StagingArea();
            Commit initialCommit = new Commit(null, "Initialization Commit", Helper.currDateTime());

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
     * 5) if it is deleted, mark the file as "deleted".
     * 6) if the file is in staging area but not in current commit, do the same checks.
     * 7) Finally, show untracked file. */
    public static void status() {
        if (isRepo()) {
            System.out.println("Not a valid repo");
            return;
        }

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

        for (Map.Entry<String, String> file : starFiles.entrySet()) {
            if (commitFiles.containsKey(file.getKey())) { continue; }

            Path fileLocation = Path.of(file.getKey());
            if (Files.exists(fileLocation)) {
                if (!Helper.sha1Checker(fileLocation, file.getValue())) {
                    modified.add(file.getKey());
                }
            } else {
                deleted.add(file.getKey());
            }
        }

        System.out.println("To be commited:");
        for (Map.Entry<String, String> file : starFiles.entrySet()) {
            if (modified.contains(file.getKey()) || deleted.contains(file.getKey())) { continue; }

            System.out.println(file.getKey());
        }

        System.out.println("Modifications not staged:");
        for (String s : modified) {
            System.out.println(s);
        }

        System.out.println("Deleted:");
        for (String d : modified) {
            System.out.println(d);
        }
    }

    /** add- it receives the file location as the input.
     * it has the following jobs,
     * 1) if it doesn't exist, check if the file is not present in both the current commit and staging area.
     * 2) if they don't have it either, the output is "File not found".
     * 3) if the file doesn't exist in directory but exists in either current commit or staging area,
     * change the value of the file location's map value to "deleted" (if it already exists in staging area)
     * or create a new entry in the staging area's map with the key as the file location and value as
     * "deleted" (if it does not exist in staging area but does in the commit).
     * 4) compute the file's hash.
     * 5) if the file's hash is the same as the one in commit, don't add it in the staging area
     * and remove it from staging area if it has an entry there (due to modifications which were then changed back).
     * 6) if the hash is different from the one in the commit, make an entry of the file in the staging area or
     * overwrite it if already present.*/
    public static void add(String file) {
        if (isRepo()) {
            System.out.println("Not a valid repo");
            return;
        }

        StagingArea star = (StagingArea) Helper.objectRead(".mini-vcs/stagingArea.txt");
        String path = ".mini-vcs/commit/" + star.getCurr();
        Commit curr = (Commit) Helper.objectRead(path);

        Map<String, String> starFiles = star.getFiles();
        Map<String,String> commitFiles = curr.getFiles();

        Path filePath = Path.of(file);

        boolean b = Files.exists(filePath);

        if (!b && !commitFiles.containsKey(file) && !starFiles.containsKey(file)) {
            System.out.println("File does not exist");
            return;
        }

        if (!b) {
            if (starFiles.containsKey(file)) {
                starFiles.replace(file, "deleted");
            } else {
                starFiles.put(file, "deleted");
            }
            System.out.println("File: " + file + "staged for deletion");
            Helper.objectWrite(star ,".mini-vcs/stagingArea.txt");
            return;
        }

        String hash = Helper.sha1(filePath);

        if (!Helper.sha1Checker(filePath, commitFiles.get(file))) {
            if (starFiles.containsKey(file)) {
                starFiles.replace(file, hash);
            } else {
                starFiles.put(file, hash);
            }
            System.out.println("File: " + file + "staged for commit");
            Helper.objectWrite(star ,".mini-vcs/stagingArea.txt");
        }
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
