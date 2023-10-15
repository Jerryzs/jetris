package ui;

public class Main {
    public static void main(String[] args) {
        int refreshRate = 30;
        if (args.length > 0) {
            refreshRate = Integer.parseInt(args[0]);
        }

        new CLI(System.in, System.out, refreshRate);
    }
}
