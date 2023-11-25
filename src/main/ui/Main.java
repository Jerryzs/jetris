package ui;

public class Main {
    public static void main(String[] args) {
        int framerate = 60;
        if (args.length > 0) {
            framerate = Integer.parseInt(args[0]);
        }

//        new CLI(System.in, System.out, framerate);
        new GUI(framerate);
    }
}
