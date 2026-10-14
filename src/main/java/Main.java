/*
init
status
add
commit
checkout
log
 */

public class Main {
    public static void main(String[] args) {
        String def = "Invalid operation, use the following commands " +
                "\ninit " +
                "\nstatus " +
                "\ncommit \"message\" " +
                "\nadd \"filename\" " +
                "\ncheckout \"commit id\" " +
                "\nlog";

        if (args.length == 0) {
            System.out.println(def);
            return;
        }
        else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("commit")
                || args[0].equalsIgnoreCase("checkout")) {
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

            case "checkout" -> Repo.checkout(args[1]);

            case "log" -> Repo.log();

            default -> System.out.println(def);
        }

    }
}
