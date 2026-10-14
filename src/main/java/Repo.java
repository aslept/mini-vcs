import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Repo {
    private static boolean isRepo() {
        return Files.exists(Path.of(".mini-vcs"));
    }

    private static Path repo = Path.of(".mini-vcs");
    private static Path commits = Path.of(".mini-vcs/commits");
    private static Path blobs = Path.of(".mini-vcs/blob");
    private static Path tempDir = Path.of(".mini-vcs/tempDir");
    private static Path tempFile = Path.of(".mini-vcs/tempDir/tempFile.txt");
    private static Path stageFolder = Path.of(".mini-vcs/stageFolder");

    /** init- creates the repo folder and necessary folders and creates an empty commit */
    public static void init() {
        if (Files.exists(repo)) {
            System.out.println("Repository already exists!");
            return;
        }

        try {
            Files.createDirectory(repo);
            Files.createDirectory(commits);
            Files.createDirectory(blobs);
            Files.createDirectory(tempDir);
            Files.createDirectory(stageFolder);

            StagingArea stagingArea = new StagingArea();
            Commit initialCommit = new Commit(null, "Initialization Commit", Helper.currDateTime());

            Helper.objectWrite(initialCommit, tempFile.toString());
            String hex = Helper.sha1(tempFile);
            String path = ".mini-vcs/commits/" + hex + ".txt";
            Helper.objectWrite(initialCommit, path);

            stagingArea.setCurr(hex  + ".txt");
            Helper.objectWrite(stagingArea, ".mini-vcs/stagingArea.txt");

            Files.delete(tempFile);

            System.out.println("Repository successfully initialized!");
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
        if (!isRepo()) {
            System.out.println("Not a valid repo");
            return;
        }

        StagingArea star = (StagingArea) Helper.objectRead(".mini-vcs/stagingArea.txt");
        String path = ".mini-vcs/commits/" + star.getCurr();
        Commit curr = (Commit) Helper.objectRead(path);

        Map<String, String> starFiles = star.getFiles();
        Map<String,String> commitFiles = curr.getFiles();

        List<String> modified = new ArrayList<>();
        List<String> deleted = new ArrayList<>();

        // file in previous commit.
        for (Map.Entry<String, String> file : commitFiles.entrySet()) {
            String fileLocation = file.getKey();

            // file in staging area
            if (starFiles.containsKey(fileLocation)) {
                String hexValue = starFiles.get(fileLocation);

                if (Files.exists(Path.of(fileLocation))) {
                    String fileHex = Helper.sha1(Path.of(fileLocation));
                    if (!fileHex.equalsIgnoreCase(hexValue)) { modified.add(fileLocation); }
                } else {
                    deleted.add(fileLocation);
                }
            }
            // file not in staging area
            else {
                String hexValue = file.getValue();

                if (Files.exists(Path.of(fileLocation))) {
                    String fileHex = Helper.sha1(Path.of(fileLocation));
                    if (!fileHex.equalsIgnoreCase(hexValue)) { modified.add(fileLocation); }
                } else {
                    deleted.add(fileLocation);
                }
            }
        }

        // file only in staging area
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
            System.out.println(file.getKey());
        }

        System.out.println("Modifications not staged:");
        for (String s : modified) {
            System.out.println(s);
        }

        System.out.println("Deleted:");
        for (String d : deleted) {
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
        if (!isRepo()) {
            System.out.println("Not a valid repo");
            return;
        }

        StagingArea star = (StagingArea) Helper.objectRead(".mini-vcs/stagingArea.txt");
        String path = ".mini-vcs/commits/" + star.getCurr();
        Commit curr = (Commit) Helper.objectRead(path);

        Map<String, String> starFiles = star.getFiles();
        Map<String,String> commitFiles = curr.getFiles();

        Path filePath = Path.of(file);

        if (Files.isDirectory(filePath)) {
            System.out.println("Not a file.");
            return;
        }

        boolean b = Files.exists(filePath);

        if (!b) {
            if (!commitFiles.containsKey(file)) {
                if (!starFiles.containsKey(file)) {
                    System.out.println("File does not exist");
                    return;
                }
            }
        }

        if (!b) {
            if (starFiles.containsKey(file)) {
                starFiles.replace(file, "deleted");
            } else {
                starFiles.put(file, "deleted");
            }
            System.out.println("File: " + file + " staged for deletion");
            Helper.objectWrite(star ,".mini-vcs/stagingArea.txt");
            return;
        }

        String hash = Helper.sha1(filePath);

        if (Helper.sha1Checker(filePath, commitFiles.get(file))) {
            if (starFiles.containsKey(file)) {
                starFiles.remove(file);
                System.out.println("File: " + file + " is the same in the latest commit, removing from staging area");
            } else {
                System.out.println("File: " + file + " is the same in the latest commit");
            }
            Helper.objectWrite(star ,".mini-vcs/stagingArea.txt");
            return;
        }

        if (!Helper.sha1Checker(filePath, commitFiles.get(file))) {
            if (starFiles.containsKey(file)) {
                starFiles.replace(file, hash);
            } else {
                starFiles.put(file, hash);
            }
            star.update(filePath.toString(), hash);
            System.out.println("File: " + file + " staged for commit");
            Helper.objectWrite(star ,".mini-vcs/stagingArea.txt");
        }
    }

    /** commit- working of the commit method,
     * 1) creates a new hashmap and puts all the elements of the last commit in it.
     * 2) replace the common keys' values (last commit and staging area) to the one in the latter.
     * 3) add the keys which are new in the staging area to the new map.
     * 4) create a commit with this map.
     * 5) save the files in the blob folder with their hashcode names.
     * 6) update the current commit in staging area to this one and clear its map.*/
    public static void commit(String message) {
        if (!isRepo()) {
            System.out.println("Not a valid repo");
            return;
        }

        StagingArea star = (StagingArea) Helper.objectRead(".mini-vcs/stagingArea.txt");
        String path = ".mini-vcs/commits/" + star.getCurr();
        Commit curr = (Commit) Helper.objectRead(path);

        Map<String, String> starFiles = star.getFiles();
        Map<String,String> commitFiles = curr.getFiles();

        Map<String, String> latest = new TreeMap<>();
        latest.putAll(commitFiles);
        latest.putAll(starFiles);

        latest.entrySet().removeIf(e -> e.getValue().equalsIgnoreCase("deleted"));

        /* The map has the full location of the file, but when saving in repo, we strip
        *  the path and only save the file by the name of the hash.*/

        for (Map.Entry<String, String> fileLocation : latest.entrySet()) {
            String fileName = Path.of(fileLocation.getKey()).getFileName().toString();

            if (Files.exists(Path.of(".mini-vcs/blob/" + fileLocation.getValue()))) {
                continue;
            }

            String filePath = ".mini-vcs/blob/" + fileLocation.getValue();

            try {
                Files.copy(Path.of(".mini-vcs/stageFolder/" + fileName), Path.of(filePath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Commit latestCommit = new Commit(star.getCurr(), message, Helper.currDateTime());
        latestCommit.setFiles(latest);

        Helper.objectWrite(latestCommit, tempFile.toString());
        String hex = Helper.sha1(tempFile);
        String file = ".mini-vcs/commits/" + hex + ".txt";
        Helper.objectWrite(latestCommit, file);

        star.setCurr(hex + ".txt");
        star.deleteStage();
        starFiles.clear();
        Helper.objectWrite(star, ".mini-vcs/stagingArea.txt");

        try {
            Files.delete(tempFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Commit successful! Commit ID: " + hex);

    }

    /** checkout- creates a folder with the name of the format: Commit "first 6 digits of the commit hash"
     * and copies the files from the blob with the location as given in the commit map with this folder as the parent.*/
    public static void checkout(String commitHex) {
        if (!isRepo()) {
            System.out.println("Not a valid repo");
            return;
        }

        if (!Files.exists(Path.of(".mini-vcs/commits/" + commitHex + ".txt"))) {
            System.out.println("No such commit hash.");
            return;
        }

        String newFolder = "Commit " + commitHex.substring(0, 6);

        try {
            Files.createDirectory(Path.of(newFolder));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path path = Path.of(".mini-vcs/commits/" + commitHex + ".txt");
        Commit commit = (Commit) Helper.objectRead(path.toString());

        Map<String, String> commitFiles = commit.getFiles();

        for (Map.Entry<String, String> fileLocation : commitFiles.entrySet()) {
            File file = new File(newFolder + "/" + fileLocation.getKey());
            String parent = file.getParent();
            try {
                Files.createDirectories(Path.of(parent));
                Files.copy(Path.of(".mini-vcs/blob/" + fileLocation.getValue()), Path.of(file.getPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** log- it displays information about all the commits (including the initial commit made by init command) in a formatted way. */
    public static void log() {
        if (!isRepo()) {
            System.out.println("Not a valid repo");
            return;
        }

        StagingArea star = (StagingArea) Helper.objectRead(".mini-vcs/stagingArea.txt");
        String path = ".mini-vcs/commits/" + star.getCurr();
        Commit curr = (Commit) Helper.objectRead(path);
        String commitID =  star.getCurr().replace(".txt", "");

        while (curr != null) {
            String message = curr.getMessage();
            String dateAndTime = curr.getDateAndTime();

            String logMessage = "Commit ID: " + commitID
                    + "\nMessage: " + message
                    + "\nDate: " + dateAndTime
                    + "\n";

            System.out.println(logMessage);

            if (curr.getParent() != null) {
            commitID = curr.getParent().replace(".txt", "");
            }

            if (curr.getParent() == null) {
                curr = null;
                continue;
            }

            curr = (Commit) Helper.objectRead(".mini-vcs/commits/" + curr.getParent());
        }


    }
}

class StagingArea implements Serializable {
    private String currName;
    private Map<String, String> files = new TreeMap<>();

    public void setCurr(String curr) {
        this.currName = curr;
    }

    public void deleteStage() {
        deleteDirectory(new File(".mini-vcs/stageFolder"));

        try {
            Files.createDirectory(Path.of(".mini-vcs/stageFolder/"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCurr() {
        return currName;
    }

    public Map<String, String> getFiles() {
        return files;
    }

    /* updates and replaces the file in the staging area with the file in the system in the map.
    *  saves the file by its name only and stripping the full path */
    public void update(String filePath, String inputHash) {
        String hash = Helper.sha1(Path.of(filePath));
        String fileName = Path.of(filePath).getFileName().toString();

        if (Helper.sha1Checker(Path.of(filePath), inputHash)) {
            try {
                Files.copy(Path.of(filePath), Path.of(".mini-vcs/stageFolder/" + fileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
