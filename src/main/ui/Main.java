package ui;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            int refreshRate = 30;
            if (args.length > 0) {
                refreshRate = Integer.parseInt(args[0]);
            }

            new CLI(System.in, System.out, refreshRate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
