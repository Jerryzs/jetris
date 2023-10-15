package model;

import model.tetromino.AbstractTetromino;

import java.util.*;

public class Game extends TimerTask {
    public static final int FRAMERATE = 50;
    private static final int LOCK_FRAME_COUNTER_RESET_LIMIT = 6;

    private final Playfield playfield;
    private final RandomBag bag;

    private AbstractTetromino hold;
    private boolean holdingAllowed = true;

    private final double gravity;

    private double moveCells;
    private int lockFrameCounter;
    private int lockFrameCounterResetCounter;

    private boolean paused;
    private boolean over;

    private final Runnable updateInput;

    public Game() {
        this(null);
    }

    public Game(Runnable updateInput) {
        this(updateInput, new Playfield(), new RandomBag(), null);
    }

    public Game(Runnable updateInput, Playfield playfield, RandomBag bag, AbstractTetromino spawn) {
        this.updateInput = updateInput;
        this.playfield = playfield;
        this.bag = bag;

        this.playfield.spawn(spawn == null ? bag.pop() : spawn);

        this.gravity = 0.01667;

        Timer timer = new Timer();
        timer.schedule(this, 0, 1000 / Game.FRAMERATE);

    }

    public void toggleGame() {
        this.paused = !this.paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isOver() {
        return this.over;
    }

    public AbstractTetromino getHold() {
        return this.hold;
    }

    public List<AbstractTetromino> getPreview() {
        List<AbstractTetromino> preview = this.bag.getPreview();

        if (this.playfield.getCurrent() != null && this.playfield.getCurrent().isHidden()) {
            preview.add(0, this.playfield.getCurrent());
            preview.remove(preview.size() - 1);
            return preview;
        }

        return preview;
    }

    public int get(int x, int y) {
        return this.playfield.get(x, y);
    }

    public void hold() {
        if (!this.holdingAllowed) {
            return;
        }

        this.lockFrameCounter = 0;
        this.lockFrameCounterResetCounter = 0;
        this.moveCells = 0;

        this.hold = this.playfield.swapCurrent(this.hold == null ? this.bag.pop() : this.hold);
        this.hold.reset();
        this.holdingAllowed = false;
    }

    public void softDrop() {
        this.moveCells = 2;
    }

    public void hardDrop() {
        for (int i = 0; i < 20; i++) {
            this.playfield.move(AbstractTetromino.Direction.DOWN);
        }
        this.lockdown();
    }

    public void moveLeft() {
        if (this.playfield.move(AbstractTetromino.Direction.LEFT)) {
            this.onMoveSuccessful();
        }
    }

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

        if (this.lockFrameCounter > 0) {
            this.lockFrameCounter = 0;
        }

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

        this.moveCells = 1 / (21600 * this.gravity * Math.pow(Game.FRAMERATE, 3));
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

        this.moveCells += this.gravity * 60 / Game.FRAMERATE;

        if (this.playfield.isReadyToLock()) {
            this.lockFrameCounter++;

            if (this.lockFrameCounter >= Game.FRAMERATE / 2
                    || this.lockFrameCounterResetCounter >= Game.LOCK_FRAME_COUNTER_RESET_LIMIT) {
                this.lockdown();
            }
        }

        if (this.updateInput != null) {
            this.updateInput.run();
        }
    }
}
