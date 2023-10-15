package model.tetromino;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractTetromino {
    private static int count;

    private int test = -1;
    private int direction;

    protected Direction orientation;
    protected int id;
    protected int[] coords;

    protected AbstractTetromino() {
        this.id = ++count;
        this.reset(true);
    }

    public void reset() {
        this.reset(false);
    }

    private void reset(boolean toBuffer) {
        this.orientation = Direction.DOWN;
        this.coords = new int[]{4, toBuffer ? 20 : 18};
    }

    public int getId() {
        return this.id;
    }

    public int[] getCoords() {
        return new int[]{this.coords[0], this.coords[1]};
    }

    public boolean isHidden() {
        return this.coords[1] == 20;
    }

    public Set<Integer> testMove(Direction direction) {
        if (direction == Direction.UP) {
            throw new IllegalArgumentException("Cannot move up.");
        }

        int[] newCoords = new int[2];
        System.arraycopy(this.coords, 0, newCoords, 0, 2);

        AbstractTetromino.move(newCoords, direction);

        return AbstractTetromino.findAbsoluteCoords(newCoords, this.getRelative());
    }

    /**
     * REQUIRES: there must be space for the Tetromino to move to
     */
    public void move(Direction direction) {
        AbstractTetromino.move(this.coords, direction);
    }

    public static void move(int[] coords, Direction direction) {
        switch (direction) {
            case DOWN:
                coords[1]--;
                break;
            case LEFT:
                coords[0]--;
                break;
            case RIGHT:
                coords[0]++;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public Set<Integer> testRotate(int direction) {
        return this.test >= 4 ? null : this.getRotation(++this.test, this.direction = direction);
    }

    public void rotate() {
        if (this.direction != 1 && this.direction != -1) {
            throw new IllegalStateException("Must be called after testing at least 1 rotation.");
        }

        int[] translation = (direction == 1 ? this.getRightKick() : this.getLeftKick())
                [this.orientation.ordinal()][test];
        this.coords[0] += translation[0];
        this.coords[1] += translation[1];

        this.orientation = this.orientation.get(this.direction);

        this.test = -1;
        this.direction = 0;
    }

    protected Set<Integer> getRotation(int test, int direction) {
        Direction no = this.orientation.get(direction);
        int[] translation = (direction == 1 ? this.getRightKick() : this.getLeftKick())
                [this.orientation.ordinal()][test];

        Set<Integer> occupiedCoords = new HashSet<Integer>();

        for (int c : this.getRelative(no)) {
            int[] rc = AbstractTetromino.coords(c);
            occupiedCoords.add(AbstractTetromino.num(
                    this.coords[0] + translation[0] + rc[0],
                    this.coords[1] + translation[1] + rc[1]));
        }

        return occupiedCoords;
    }

    protected abstract int[][][] getLeftKick();

    protected abstract int[][][] getRightKick();

    public boolean occupies(int x, int y) {
        return this.occupies(AbstractTetromino.num(x, y));
    }

    public boolean occupies(int n) {
        return this.occupies().contains(n);
    }

    public Set<Integer> occupies() {
        return this.findAbsoluteCoords(this.getRelative());
    }

    /**
     * The 8-integer array representation of this Tetromino's default state.
     * The default state of a Tetromino occupies at most a 2x4 area, therefore
     * the first 4 integers corresponds to the first row, while the last 4
     * corresponds to the second row. A Tetromino block is occupying the column
     * if the integer is 1; otherwise, the integer is 0;
     *
     * @return an array of 8 integers that represents the Tetromino in its default state.
     */
    public abstract int[] getStandalone();

    public Set<Integer> getRelative() {
        return this.getRelative(this.orientation);
    }

    public abstract Set<Integer> getRelative(Direction orientation);

    public Set<Integer> findAbsoluteCoords(Set<Integer> relativeCoords) {
        return AbstractTetromino.findAbsoluteCoords(this.coords, relativeCoords);
    }

    public static Set<Integer> findAbsoluteCoords(int[] coords, Set<Integer> relativeCoords) {
        Set<Integer> absoluteCoords = new HashSet<Integer>();

        for (int r : relativeCoords) {
            int[] rc = AbstractTetromino.coords(r);
            absoluteCoords.add(AbstractTetromino.num(coords[0] + rc[0], coords[1] + rc[1]));
        }

        return absoluteCoords;
    }

    public static int[] coords(int num) {
        return new int[]{
                (num / 100000 == 1 ? -1 : 1) * (num % 100000) / 1000,
                ((num % 1000) / 100 == 1 ? -1 : 1) * (num % 100)
        };
    }

    public static int num(int... coords) {
        return (coords[0] < 0 ? 100000 : 0) + Math.abs(coords[0]) * 1000
                + (coords[1] < 0 ? 100 : 0) + Math.abs(coords[1]);
    }

    public enum Direction {
        DOWN,
        LEFT,
        UP,
        RIGHT;

        private static final Direction[] VALUES = Direction.values();

        public Direction get(int offset) {
            return Direction.VALUES[(Direction.VALUES.length + this.ordinal() + offset) % Direction.VALUES.length];
        }
    }
}
