package com.jerryzs.jetris.persistence;

import java.io.*;

public class HighScore {
    private static final String DEFAULT_PATH = "./record";
    private static HighScore INSTANCE = null;

    private final File file;
    private int score;

    private HighScore(String path) {
        this.file = new File(path);
        this.load();
    }

    public int get() {
        return this.score;
    }

    public void set(int score) {
        this.score = score;
        this.save();
    }

    private void load() {
        if (!(this.file.exists() && this.file.isFile() && this.file.canRead())) return;

        try (DataInputStream in = new DataInputStream(new FileInputStream(this.file))) {
            this.score = in.readInt();
        } catch (NumberFormatException | IOException e) {
            // ignore
        }
    }

    private void save() {
        if (this.score == 0) return;

        try {
            if (!this.file.createNewFile() && !(this.file.isFile() && this.file.canWrite())) return;
        } catch (IOException e) {
            return;
        }

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(this.file))) {
            out.writeInt(this.score);
        } catch (IOException e) {
            // ignore
        }
    }

    public static HighScore instance(String path) {
        if (HighScore.INSTANCE == null) return HighScore.INSTANCE = new HighScore(path);
        else return HighScore.INSTANCE;
    }

    public static HighScore instance() {
        return HighScore.instance(HighScore.DEFAULT_PATH);
    }
}
