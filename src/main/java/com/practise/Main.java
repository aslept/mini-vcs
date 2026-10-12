package com.practise;

/*
init
status
add
commit
log
checkout
 */

public class Main {
    public static void main(String[] args) {
        String def = "Invalid operation, use the following commands \ninit \nstatus \ncommit \"message\" \nadd \"filename\"";

        if (args.length == 0) {
            System.out.println(def);
            return;
        }
        else if (args[0].equals("add") || args[0].equals("commit")) {
            if (args.length == 1 || args.length > 2) {
                System.out.println(def);
                return;
            }
        }

        switch (args[0]) {
            case "init" -> Repo.init();

            case "status" -> Repo.status();

            case "add" -> Repo.add(args[1]);

            case "commit" -> Repo.commit(args[1]);

            default -> System.out.println(def);
        }

    }
}
