package model;

import model.tetromino.AbstractTetromino;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Game extends TimerTask {
    private static final int LOCK_FRAME_COUNTER_RESET_LIMIT = 8;

    public final int framerate;

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

    private final Runnable draw;

    public Game() {
        this(null, 30);
    }

    public Game(Runnable draw, int framerate) {
        this(draw, framerate, new Playfield(), new RandomBag(), null);
    }

    public Game(Runnable draw, int framerate, Playfield playfield, RandomBag bag, AbstractTetromino spawn) {
        this.draw = draw;
        this.framerate = framerate;
        this.playfield = playfield;
        this.bag = bag;

        this.playfield.spawn(spawn == null ? bag.pop() : spawn);

        this.gravity = 0.01667;

        Timer timer = new Timer();
        timer.schedule(this, 0, 1000 / this.framerate);

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

        AbstractTetromino current = this.playfield.getCurrent();
        if (current != null && current.isHidden()) {
            preview.add(0, current);
            preview.remove(preview.size() - 1);
            return preview;
        }

        return preview;
    }

    public int get(int x, int y) {
        return this.playfield.get(x, y);
    }

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

    public void softDrop() {
        this.moveCells = 1;
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
        if (this.draw != null) {
            this.draw.run();
        }

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
