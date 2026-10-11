package com.practise;

/*
init
status
commit
add
log
checkout
 */

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("empty bruh?");
            return;
        }

        switch (args[0]) {
            case "init" -> Repo.init();

            case "status" -> Repo.status();

            case "add" -> Repo.add(args[1]);
        }
    }
}
