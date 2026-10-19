# Mini-VCS
A simple tool that stores versions of files
using SHA-1 hashing in a linear fashion.

## Commands
Compile it using:
``` 
mvn compile
```

The following commands are supported:
```
java @run init
```

Starts the repository by creating the necessary folders and making an empty
initialization commit.

```
java @run status
```

Lists the files in the staging area which will be commited,
the tracked files which were modified since the last commit or from the staging cache.
the tracked files which are deleted (adding them will stop the tracking).

```
java @run add "file location"
```

Adds untracked and modified files in the staging cache.
Adding a deleted file which was tracked in the last commit or in the staging area
will stage the file for deletion, halting its tracking.

```
java @run commit "message"
```

Saves the snapshot of the tracked files as they are present in the staging area
or the last commit (if no modifications staged).
Files staged for deletion will be removed from the tracking list.

```
java @run checkout "commit ID"
```

Creates a new folder with the format as "Commit" followed by the first 6 digits of the commit ID
and copies the files in it as they were in that snapshot.

```
java @run log
```

List the commits and their information, starting from the latest commit, followed by its parent
and so on.

Note- On Windows put @run inside quotation. 
```
java "@run" commit "message"
```