package ui;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            new CLI(System.in, System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
