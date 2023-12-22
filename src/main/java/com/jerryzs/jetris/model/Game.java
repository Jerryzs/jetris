package com.jerryzs.jetris.model;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * A Game of Jetris.
 */
public class Game implements Runnable {
    private static final int LOCK_FRAME_COUNTER_RESET_LIMIT = 8;

    /**
     * An unmodifiable list of gravity values associated with levels of the
     * game, i.e., <code>Game.GRAVITY.get(level - 1)</code> retrieves the
     * gravity value of the given <code>level</code>, for <code>1 <= level <=
     * 15</code>.
     */
    private static final List<Double> GRAVITY = List.of(
            0.01667,
            0.021017,
            0.026977,
            0.035256,
            0.04693,
            0.06361,
            0.0879,
            0.1236,
            0.1775,
            0.2598,
            0.388,
            0.59,
            0.92,
            1.46,
            2.36
    );

    private final Playfield playfield;
    private final RandomBag bag;
    private final Score score;

    private int framerate;

    private Tetromino hold;
    private boolean holdingAllowed;

    private double moveCells;
    private int lockFrameCounter;
    private int lockFrameCounterResetCounter;

    private boolean paused;
    private boolean over;

    /**
     * Create a new game of Jetris with the specified framerate.
     * <p>
     * REQUIRES: framerate > 0
     *
     * @param framerate The number of times the game is updated per second
     */
    public Game(int framerate) {
        this(framerate, new Playfield(), new RandomBag(), null, null, true);
    }

    private Game(int framerate, Playfield playfield, RandomBag bag,
            Tetromino spawn, Tetromino hold, boolean holdingAllowed) {
        this(framerate, playfield, bag, new Score(playfield), spawn, hold, holdingAllowed);
    }

    /**
     * Create a new game of Jetris with the specified framerate, playfield
     * object, 7-bag object, and tetromino piece to spawn. This constructor is
     * exposed primarily for <b>DEBUGGING</b> and should not be used for any
     * other purposes.
     * <p>
     * REQUIRES: framerate > 0 and playfield != null and bag != null
     *
     * @param framerate The number of times the game is updated per second
     * @param playfield The playfield object for the game
     * @param bag       The 7-bag object for the game
     * @param spawn     The first tetromino piece to spawn; or null
     */
    protected Game(int framerate, Playfield playfield, RandomBag bag, Score score,
            Tetromino spawn, Tetromino hold, boolean holdingAllowed) {
        this.framerate = framerate;
        this.playfield = playfield;
        this.bag = bag;
        this.score = score;

        this.playfield.spawn(spawn == null ? bag.pop() : spawn);

        this.hold = hold;
        this.holdingAllowed = holdingAllowed;
    }

    public static Game fromJson(JSONObject json, int framerate) throws IOException {
        Playfield playfield = Playfield.fromJsonArray(json.getJSONArray("matrix"));

        return new Game(
                framerate,
                playfield,
                RandomBag.fromJsonArray(json.getJSONArray("bag")),
                Score.fromJson(json.getJSONObject("score"), playfield),
                json.has("current") ? Tetromino.fromJson(json.getJSONObject("current")) : null,
                json.has("hold") ? new Tetromino(Tetromino.Type.valueOf(json.getString("hold")), true) : null,
                json.getBoolean("holdingAllowed")
        );
    }

    public static JSONObject toJson(Game game) {
        JSONObject obj = new JSONObject();

        obj.put("current", Tetromino.toJson(game.getPlayfield().getCurrent()));
        obj.put("hold", game.getHold().getType().name());
        obj.put("holdingAllowed", game.getHoldingAllowed());
        obj.put("bag", RandomBag.toJsonArray(game.bag));
        obj.put("score", Score.toJson(game.score));
        obj.put("matrix", Playfield.toJsonArray(game.playfield));

        return obj;
    }

    /**
     * EFFECTS: Set the framerate of the game. This method should primarily be
     * used to dynamically update the framerate with the actual number of times
     * the game loop is executed every second.
     *
     * @param framerate The actual measured framerate
     */
    public void framerate(int framerate) {
        this.framerate = framerate;
    }

    /**
     * EFFECTS: Get the current framerate used for the timings of the game.
     *
     * @return The current framerate
     */
    public int framerate() {
        return this.framerate;
    }

    public double getGravity() {
        return Game.GRAVITY.get(this.score.getLevel() - 1);
    }

    public Playfield getPlayfield() {
        return this.playfield;
    }

    public Score getScore() {
        return this.score;
    }

    /**
     * MODIFIES: this
     * <p>
     * EFFECTS: Pause or unpause the game.
     */
    public void toggleGame() {
        this.paused = !this.paused;
    }

    /**
     * EFFECTS: Get whether the game is paused.
     *
     * @return True if the game is paused or is over; otherwise, false
     */
    public boolean isPaused() {
        return this.paused;
    }

    /**
     * EFFECTS: Get whether the game is over. Any reference to this game object
     * may safely be removed if this method returns true.
     *
     * @return True if the game is over; otherwise, false
     */
    public boolean isOver() {
        return this.over;
    }

    /**
     * EFFECTS: Get the tetromino object that is being held by the user.
     *
     * @return The tetromino object held
     */
    public Tetromino getHold() {
        return this.hold;
    }

    public boolean getHoldingAllowed() {
        return this.holdingAllowed;
    }

    /**
     * EFFECTS: Get a preview of the first 5 tetrominoes in the 7-bag or, if the
     * current tetromino is hidden in the buffer, the hidden tetromino and the
     * first 4 tetrominoes in the 7-bag.
     *
     * @return A List of 5 tetrominoes
     */
    public List<Tetromino> getPreview() {
        List<Tetromino> preview = this.bag.getPreview();

        Tetromino current = this.playfield.getCurrent();
        if (current != null && current.isHidden()) {
            preview.add(0, current);
            preview.remove(preview.size() - 1);
            return preview;
        }

        return preview;
    }

    public Iterator<Tetromino> getBagIterator() {
        return this.bag.getIterator();
    }

    /**
     * REQUIRES: 0 <= x <= 9 and 0 <= y <= 21
     * <p>
     * EFFECTS: Get the id of the tetromino at the specified coordinate in the
     * playfield.
     *
     * @param x The x-coordinate, with 0 being the leftmost and 9 being the
     *          rightmost column of the playfield
     * @param y The y-coordinate, with 0 being the bottom, 19 being the top row
     *          of the playfield, and 20-21 being the buffer area
     * @return The id of the tetromino occupying the coordinate or 0 if the
     * coordinate is not occupied
     */
    public int get(int x, int y) {
        return this.playfield.get(x, y);
    }

    /**
     * MODIFIES: this, this.playfield
     * <p>
     * EFFECTS: Put the currently dropping tetromino on hold, if allowed, and
     * start dropping the previously held tetromino, if applicable.
     *
     * @return False if the current tetromino is not allowed to be held and is,
     * therefore, not held; otherwise, true
     */
    public boolean hold() {
        if (!this.holdingAllowed || this.playfield.getCurrent() == null || this.playfield.getCurrent().isHidden()) {
            return false;
        }

        this.lockFrameCounter = 0;
        this.lockFrameCounterResetCounter = 0;
        this.moveCells = 0;

        this.hold = this.playfield.swapCurrent(this.hold == null ? this.bag.pop() : this.hold);
        this.hold.reset();
        this.holdingAllowed = false;

        this.score.resetDropBonus();

        return true;
    }

    /**
     * MODIFIES: this
     * <p>
     * EFFECTS: Force the current tetromino to immediately drop by 1 cell in the
     * next frame.
     */
    public void softDrop() {
        this.moveCells = 1;
        this.score.dropBonus(1);
    }

    /**
     * MODIFIES: this, this.playfield
     * <p>
     * EFFECTS: Immediately drop the current tetromino to the bottom of the
     * playfield and lock it in place.
     */
    public void hardDrop() {
        while (this.playfield.move(Tetromino.Direction.DOWN)) {
            this.score.dropBonus(2);
        }
        this.lockdown();
    }

    /**
     * MODIFIES: this, this.playfield
     * <p>
     * EFFECTS: Move the current tetromino to the left by 1 cell, if possible.
     */
    public void moveLeft() {
        if (this.playfield.move(Tetromino.Direction.LEFT)) {
            this.onMoveSuccessful();
        }
    }

    /**
     * MODIFIES: this, this.playfield
     * <p>
     * EFFECTS: Move the current tetromino to the right by 1 cell, if possible.
     */
    public void moveRight() {
        if (this.playfield.move(Tetromino.Direction.RIGHT)) {
            this.onMoveSuccessful();
        }
    }

    public void rotateLeft() {
        if (this.playfield.rotate(-1)) {
            this.onMoveSuccessful();
        }
    }

    public void rotateRight() {
        if (this.playfield.rotate(1)) {
            this.onMoveSuccessful();
        }
    }

    private void onMoveSuccessful() {
        this.lockFrameCounterResetCounter++;
        this.lockFrameCounter = 0;

        if (this.lockFrameCounterResetCounter < Game.LOCK_FRAME_COUNTER_RESET_LIMIT) {
            this.moveCells = 0;
        }
    }

    private void lockdown() {
        this.score.tspinCheck();
        this.score.clear(this.playfield.lockdown());

        if (!this.playfield.spawn(this.bag.pop())) {
            this.paused = true;
            this.over = true;
        } else {
            this.holdingAllowed = true;
            this.moveCells = 1 / (21600 * this.getGravity() * Math.pow(this.framerate, 3));
        }

        this.score.settle();
    }

    @Override
    public void run() {
        if (paused) {
            return;
        }

        if (moveCells >= 1) {
            this.moveCells--;
            if (this.playfield.move(Tetromino.Direction.DOWN)) {
                this.lockFrameCounterResetCounter = 0;
                this.lockFrameCounter = 0;
            }
        }

        this.moveCells += this.getGravity() * 60 / this.framerate;

        if (this.playfield.isReadyToLock()) {
            this.lockFrameCounter++;

            if (this.lockFrameCounter >= this.framerate / 2
                    || this.lockFrameCounterResetCounter >= Game.LOCK_FRAME_COUNTER_RESET_LIMIT) {
                this.lockdown();
            }
        }
    }
}
