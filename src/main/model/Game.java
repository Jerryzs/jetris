package model;

import model.tetromino.AbstractTetromino;

import java.util.Iterator;
import java.util.List;

/**
 * A Game of Jetris.
 */
public class Game implements Runnable {
    private static final int LOCK_FRAME_COUNTER_RESET_LIMIT = 8;

    private final Playfield playfield;
    private final RandomBag bag;

    private int framerate;

    private AbstractTetromino hold;
    private boolean holdingAllowed;

    private final double gravity;

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
    public Game(int framerate, Playfield playfield, RandomBag bag,
                AbstractTetromino spawn, AbstractTetromino hold, boolean holdingAllowed) {
        this.framerate = framerate;
        this.playfield = playfield;
        this.bag = bag;

        this.playfield.spawn(spawn == null ? bag.pop() : spawn);

        this.hold = hold;
        this.holdingAllowed = holdingAllowed;

        this.gravity = 0.01667;
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
        return this.gravity;
    }

    public Playfield getPlayfield() {
        return this.playfield;
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
    public AbstractTetromino getHold() {
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
    public List<AbstractTetromino> getPreview() {
        List<AbstractTetromino> preview = this.bag.getPreview();

        AbstractTetromino current = this.playfield.getCurrent();
        if (current != null && current.isHidden()) {
            preview.add(0, current);
            preview.remove(preview.size() - 1);
            return preview;
        }

        return preview;
    }

    public Iterator<AbstractTetromino> getBagIterator() {
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
    }

    /**
     * MODIFIES: this, this.playfield
     * <p>
     * EFFECTS: Immediately drop the current tetromino to the bottom of the
     * playfield and lock it in place.
     */
    public void hardDrop() {
        for (int i = 0; i < 20; i++) {
            this.playfield.move(AbstractTetromino.Direction.DOWN);
        }
        this.lockdown();
    }

    /**
     * MODIFIES: this, this.playfield
     * <p>
     * EFFECTS: Move the current tetromino to the left by 1 cell, if possible.
     */
    public void moveLeft() {
        if (this.playfield.move(AbstractTetromino.Direction.LEFT)) {
            this.onMoveSuccessful();
        }
    }

    /**
     * MODIFIES: this, this.playfield
     * <p>
     * EFFECTS: Move the current tetromino to the right by 1 cell, if possible.
     */
    public void moveRight() {
        if (this.playfield.move(AbstractTetromino.Direction.RIGHT)) {
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
        this.playfield.lockdown();
        if (!this.playfield.spawn(this.bag.pop())) {
            this.paused = true;
            this.over = true;
            return;
        }

        this.holdingAllowed = true;

        this.moveCells = 1 / (21600 * this.gravity * Math.pow(this.framerate, 3));
    }

    @Override
    public void run() {
        if (paused) {
            return;
        }

        if (moveCells >= 1) {
            this.moveCells--;
            if (this.playfield.move(AbstractTetromino.Direction.DOWN)) {
                this.lockFrameCounterResetCounter = 0;
                this.lockFrameCounter = 0;
            }
        }

        this.moveCells += this.gravity * 60 / this.framerate;

        if (this.playfield.isReadyToLock()) {
            this.lockFrameCounter++;

            if (this.lockFrameCounter >= this.framerate / 2
                    || this.lockFrameCounterResetCounter >= Game.LOCK_FRAME_COUNTER_RESET_LIMIT) {
                this.lockdown();
            }
        }
    }
}
