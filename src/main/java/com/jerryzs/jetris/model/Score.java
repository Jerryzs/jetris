package com.jerryzs.jetris.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Score {
    private final Playfield playfield;

    private int points;
    private int level;
    private int combo;
    private int lines;

    private int backToBack;

    private int[] buffer;
    private int dropBonus;
    private int tspin;

    private int[] lastScore;

    public Score(Playfield playfield) {
        this(playfield, 1, -1, -1, 0, 0);
    }

    private Score(Playfield playfield, int level, int combo, int backToBack, int points, int lines) {
        this.playfield = playfield;
        this.level = level;
        this.combo = combo;
        this.backToBack = backToBack;
        this.points = points;
        this.lines = lines;
        this.buffer = new int[6];
    }

    public static Score fromJson(JSONObject json, Playfield playfield) throws JSONException {
        return new Score(
                playfield,
                json.getInt("level"),
                json.getInt("combo"),
                json.getInt("difficult"),
                json.getInt("points"),
                json.getInt("cleared")
        );
    }

    public static JSONObject toJson(Score score) {
        JSONObject json = new JSONObject();

        json.put("level", score.level);
        json.put("combo", score.combo);
        json.put("difficult", score.backToBack);
        json.put("points", score.points);
        json.put("cleared", score.lines);

        return json;
    }

    public int getLevel() {
        return this.level;
    }

    public int getPoints() {
        return this.points;
    }

    /**
     * EFFECTS: Return an array representing the breakdown of the settled score.
     * The following outlines the information associated with each index of the
     * array:
     * <p>
     *
     * <code>
     * [ActionScore, PerfectClearScore, ComboScore, TSpinType, BackToBackCount,
     * ComboCount]
     * </code>
     *
     * <p>
     * The sum of the first 3 integers of the array always yields the total
     * score settled.
     * <p>
     *
     * <code>TSpinType</code> is an integer from 0 to 2 that provides
     * information regarding any t-spin performed, where 0 represents no t-spin,
     * 1 a mini t-spin, and 2 a regular t-spin.
     *
     * @return An integer array representing the breakdown of the score.
     */
    public int[] getLastScore() {
        return this.lastScore;
    }

    protected void dropBonus(int points) {
        this.dropBonus += points;
    }

    protected void resetDropBonus() {
        this.dropBonus = 0;
    }

    protected void tspinCheck() {
        Tetromino t = this.playfield.getCurrent();

        if (t.getType() != Tetromino.Type.T || t.getLastOrientation() == t.getOrientation()) {
            return;
        }

        if (Math.abs(t.getLastCoords()[0] - t.getCoords()[0]) == 1
                && Math.abs(t.getLastCoords()[1] - t.getCoords()[1]) == 2) {
            this.buffer[3] = this.tspin = 2;
            return;
        }

        boolean[] corners = getCorners(t, t.getOrientation());
        this.buffer[3] = this.tspin = corners[2] && corners[3] && (corners[0] || corners[1]) ? 2
                : (corners[2] || corners[3]) && corners[0] && corners[1] ? 1
                : 0;
    }

    private boolean[] getCorners(Tetromino t, Tetromino.Direction o) {
        int ord = o.ordinal();
        boolean[] corners = new boolean[4];

        int index = 0;
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                int x = t.getCoords()[0] + (ord % 2 == 0 ? j : i) * (1 - 2 * (ord / 2));
                int y = t.getCoords()[1] + (ord % 2 == 0 ? i : j) * (1 - 2 * (ord / 2));

                if (x < 0 || x > 9 || y < 0 || y > 19) {
                    corners[index] = true;
                } else {
                    corners[index] = this.playfield.get(x, y) != 0;
                }

                index++;
            }
        }

        return corners;
    }

    protected void clear(int lineCount) {
        this.lines += lineCount;

        if (lineCount == 0) {
            this.combo = -1;

            if (this.tspin == 0) {
                this.backToBack = -1;
            } else {
                this.buffer[0] = (this.tspin == 1 ? 100 : 400) * this.level;
                this.tspin = 0;
            }

            return;
        }

        if (this.tspin != 0 || lineCount >= 4) {
            this.backToBack++;
        }

        this.buffer[0] = this.level * (lineCount == 1 ? (this.tspin == 0 ? 100 : this.tspin == 1 ? 200 : 800)
                : lineCount == 2 ? (this.tspin == 0 ? 300 : this.tspin == 1 ? 400 : 1200)
                : lineCount == 3 ? (this.tspin == 0 ? 500 : 1600) : 800);

        this.tspin = 0;

        if (this.playfield.isEmpty()) {
            this.buffer[1] = this.level * (this.backToBack > 0 ? 3200
                    : (lineCount == 1 ? 800 : lineCount == 2 ? 1200 : lineCount == 3 ? 1800 : 2000));
        }
    }

    /**
     * EFFECTS: Settle the total score from the lockdown of the current
     * tetromino piece.
     */
    protected void settle() {
        if (this.backToBack > 0) {
            this.buffer[0] = (int) Math.floor(this.buffer[0] * 1.5d);
            this.buffer[4] = this.backToBack;
        }

        if (this.combo > 0) {
            this.buffer[2] = 50 * this.combo * this.level;
            this.buffer[5] = this.combo;
        }

        this.buffer[0] += this.dropBonus;
        this.dropBonus = 0;

        int points = 0;
        for (int i = 0; i < 3; i++) {
            points += this.buffer[i];
        }

        this.points += points;

        this.level = this.lines / 10 + 1;

        this.lastScore = this.buffer;
        this.buffer = new int[6];
    }
}
