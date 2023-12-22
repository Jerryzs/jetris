package com.jerryzs.jetris.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Tetromino {
    private final Type type;

    private Direction orientation;
    private int[] coords;

    private Direction lastOrientation;
    private int[] lastCoords;

    private int test;

    public Tetromino(Type type) {
        this(type, false);
    }

    protected Tetromino(Type type, boolean held) {
        this(type, new int[]{4, held ? 18 : 20}, Direction.DOWN, new int[2], Direction.DOWN);
    }

    private Tetromino(Type type, int[] coords, Direction orientation, int[] lastCoords, Direction lastOrientation) {
        this.type = type;
        this.coords = coords;
        this.orientation = orientation;
        this.lastCoords = lastCoords;
        this.lastOrientation = lastOrientation;
    }

    /**
     * EFFECTS: Convert the state of a tetromino object into a JSON object.
     *
     * @param tetromino The tetromino to convert
     * @return The JSON object representing the tetromino
     */
    public static JSONObject toJson(Tetromino tetromino) {
        if (tetromino == null) {
            return null;
        }

        return new JSONObject()
                .put("type", tetromino.type.name())
                .put("coords", tetromino.numCoords())
                .put("orientation", tetromino.orientation.name())
                .put("lastCoords", Tetromino.num(tetromino.lastCoords))
                .put("lastOrientation", tetromino.lastOrientation);
    }

    /**
     * EFFECTS: Recover the tetromino object from a JSON object representing the
     * state of the tetromino.
     *
     * @param object The JSON object to parse
     * @return The tetromino object
     * @throws IOException If the JSON object is unreadable or if its content is
     *                     invalid
     */
    public static Tetromino fromJson(JSONObject object) throws IOException {
        try {
            return new Tetromino(
                    Tetromino.Type.valueOf(object.getString("type")),
                    Tetromino.coords(object.getInt("coords")),
                    Tetromino.Direction.valueOf(object.getString("orientation")),
                    Tetromino.coords(object.getInt("lastCoords")),
                    Tetromino.Direction.valueOf(object.getString("lastOrientation")));
        } catch (JSONException e) {
            throw new IOException();
        } catch (IllegalArgumentException e) {
            throw new AssertionError();
        }
    }

    public Type getType() {
        return this.type;
    }

    public void reset() {
        this.orientation = Direction.DOWN;
        this.coords = new int[]{4, 18};
    }

    public Direction getOrientation() {
        return this.orientation;
    }

    public int[] getLastCoords() {
        return this.lastCoords;
    }

    public Direction getLastOrientation() {
        return this.lastOrientation;
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

        Tetromino.move(newCoords, direction);

        return Tetromino.findAbsoluteCoords(newCoords, this.getRelative());
    }

    /**
     * REQUIRES: there must be space for the Tetromino to move to
     */
    public void move(Direction direction) {
        this.lastCoords = Arrays.copyOf(this.coords, 2);
        this.lastOrientation = this.orientation;

        Tetromino.move(this.coords, direction);
        this.test = 0;
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
        if (Math.abs(direction + this.test) <= Math.abs(this.test)) {
            throw new IllegalArgumentException("Subsequent tests must be in the same direction as the initial test.");
        }

        direction /= Math.abs(direction);
        return Math.abs(this.test) >= 4 ? null : this.getRotation(this.test += direction);
    }

    public void rotate() {
        if (this.test == 0) {
            throw new IllegalStateException("Must be called after testing at least 1 rotation.");
        }

        this.lastCoords = Arrays.copyOf(this.coords, 2);

        int[] translation = (this.test > 0 ? this.type.getRightKickData() : this.type.getLeftKickData())
                [this.orientation.ordinal()][Math.abs(this.test) - 1];
        this.coords[0] += translation[0];
        this.coords[1] += translation[1];

        this.lastOrientation = this.orientation;
        this.orientation = this.orientation.get(this.test / Math.abs(this.test));

        this.test = 0;
    }

    protected Set<Integer> getRotation(int test) {
        Direction no = this.orientation.get(test / Math.abs(test));
        int[] translation = (test > 0 ? this.type.getRightKickData() : this.type.getLeftKickData())
                [this.orientation.ordinal()][Math.abs(test) - 1];

        Set<Integer> occupiedCoords = new HashSet<Integer>();

        for (int c : this.type.getRelative(no)) {
            int[] rc = Tetromino.coords(c);
            occupiedCoords.add(Tetromino.num(
                    this.coords[0] + translation[0] + rc[0],
                    this.coords[1] + translation[1] + rc[1]));
        }

        return occupiedCoords;
    }

    public boolean occupies(int x, int y) {
        return this.occupies(Tetromino.num(x, y));
    }

    public boolean occupies(int n) {
        return this.occupies().contains(n);
    }

    public Set<Integer> occupies() {
        return this.findAbsoluteCoords(this.getRelative());
    }

    public Set<Integer> getRelative() {
        return this.type.getRelative(this.orientation);
    }

    public Set<Integer> findAbsoluteCoords(Set<Integer> relativeCoords) {
        return Tetromino.findAbsoluteCoords(this.coords, relativeCoords);
    }

    public static Set<Integer> findAbsoluteCoords(int[] coords, Set<Integer> relativeCoords) {
        Set<Integer> absoluteCoords = new HashSet<Integer>();

        for (int r : relativeCoords) {
            int[] rc = Tetromino.coords(r);
            absoluteCoords.add(Tetromino.num(coords[0] + rc[0], coords[1] + rc[1]));
        }

        return absoluteCoords;
    }

    public int[] getCoords() {
        return new int[]{this.coords[0], this.coords[1]};
    }

    public int numCoords() {
        return Tetromino.num(this.coords);
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

    public enum Type {
        I(
                new Color(0, 255, 255),
                new int[]{0, 0, 0, 0, 1, 1, 1, 1},
                Map.of(
                        Direction.DOWN, Set.of(101000, 0, 1000, 2000),
                        Direction.LEFT, Set.of(1001, 1000, 1101, 1102),
                        Direction.UP, Set.of(101101, 101, 1101, 2101),
                        Direction.RIGHT, Set.of(1, 0, 101, 102)),
                new int[][][]{
                        {{0, 0}, {-1, 0}, {2, 0}, {-1, 2}, {2, -1}},
                        {{0, 0}, {2, 0}, {-1, 0}, {2, 1}, {-1, -2}},
                        {{0, 0}, {1, 0}, {-2, 0}, {1, -2}, {-2, 1}},
                        {{0, 0}, {-2, 0}, {1, 0}, {-2, -1}, {1, 2}},
                },
                new int[][][]{
                        {{0, 0}, {-2, 0}, {1, 0}, {-2, -1}, {1, 2}},
                        {{0, 0}, {-1, 0}, {2, 0}, {-1, 2}, {2, -1}},
                        {{0, 0}, {2, 0}, {-1, 0}, {2, 1}, {-1, -2}},
                        {{0, 0}, {1, 0}, {-2, 0}, {1, -2}, {-2, 1}},
                }
        ),

        J(
                new Color(0, 0, 255),
                new int[]{1, 0, 0, 0, 1, 1, 1, 0},
                Map.of(
                        Direction.DOWN, Set.of(101001, 101000, 0, 1000),
                        Direction.LEFT, Set.of(1, 1001, 0, 101),
                        Direction.UP, Set.of(101000, 0, 1000, 1101),
                        Direction.RIGHT, Set.of(1, 0, 101101, 101)
                )
        ),

        L(
                new Color(255, 127, 0),
                new int[]{0, 0, 1, 0, 1, 1, 1, 0},
                Map.of(
                        Direction.DOWN, Set.of(1001, 101000, 0, 1000),
                        Direction.LEFT, Set.of(1, 0, 101, 1101),
                        Direction.UP, Set.of(101000, 0, 1000, 101101),
                        Direction.RIGHT, Set.of(101001, 1, 0, 101)
                )
        ),

        O(
                new Color(255, 255, 0),
                new int[]{0, 1, 1, 0, 0, 1, 1, 0},
                Map.of(
                        Direction.DOWN, Set.of(0, 1, 1000, 1001),
                        Direction.LEFT, Set.of(0, 1, 1000, 1001),
                        Direction.UP, Set.of(0, 1, 1000, 1001),
                        Direction.RIGHT, Set.of(0, 1, 1000, 1001)
                ),
                new int[5][4][2],
                new int[5][4][2]
        ),

        S(
                new Color(0, 255, 0),
                new int[]{0, 1, 1, 0, 1, 1, 0, 0},
                Map.of(
                        Direction.DOWN, Set.of(1, 1001, 101000, 0),
                        Direction.LEFT, Set.of(1, 0, 1000, 1101),
                        Direction.UP, Set.of(0, 1000, 101101, 101),
                        Direction.RIGHT, Set.of(101001, 101000, 0, 101)
                )
        ),

        T(
                new Color(128, 0, 128),
                new int[]{0, 1, 0, 0, 1, 1, 1, 0},
                Map.of(
                        Direction.DOWN, Set.of(1, 101000, 0, 1000),
                        Direction.LEFT, Set.of(1, 0, 1000, 101),
                        Direction.UP, Set.of(101000, 0, 1000, 101),
                        Direction.RIGHT, Set.of(1, 101000, 0, 101)
                )
        ),

        Z(
                new Color(255, 0, 0),
                new int[]{1, 1, 0, 0, 0, 1, 1, 0},
                Map.of(
                        Direction.DOWN, Set.of(101001, 1, 0, 1000),
                        Direction.LEFT, Set.of(1001, 0, 1000, 101),
                        Direction.UP, Set.of(101000, 0, 101, 1101),
                        Direction.RIGHT, Set.of(1, 101000, 0, 101101)
                )
        );

        private final int[][][] leftKickData;
        private final int[][][] rightKickData;
        private final int[] standalone;
        private final Map<Direction, Set<Integer>> relative;
        private final Color color;

        Type(Color color, int[] standalone, Map<Direction, Set<Integer>> relative) {
            this(
                    color,
                    standalone,
                    relative,
                    new int[][][]{
                            {{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}},
                            {{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}},
                            {{0, 0}, {-1, 0}, {-1, 1}, {0, -2}, {-1, -2}},
                            {{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}},
                    },
                    new int[][][]{
                            {{0, 0}, {-1, 0}, {-1, 1}, {0, -2}, {-1, -2}},
                            {{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}},
                            {{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}},
                            {{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}},
                    });
        }

        Type(Color color, int[] standalone, Map<Direction, Set<Integer>> relative,
                int[][][] leftKickData, int[][][] rightKickData) {
            this.color = color;
            this.standalone = standalone;
            this.relative = relative;
            this.leftKickData = leftKickData;
            this.rightKickData = rightKickData;
        }

        public int[][][] getLeftKickData() {
            return this.leftKickData;
        }

        public int[][][] getRightKickData() {
            return this.rightKickData;
        }

        /**
         * The 8-integer array representation of this Tetromino's default state.
         * The default state of a Tetromino occupies at most a 2x4 area,
         * therefore the first 4 integers corresponds to the first row, while
         * the last 4 corresponds to the second row. A Tetromino block is
         * occupying the column if the integer is 1; otherwise, the integer is
         * 0;
         *
         * @return an array of 8 integers that represents the Tetromino in its
         * default state.
         */
        public int[] getStandalone() {
            return this.standalone;
        }

        public Set<Integer> getRelative(Direction orientation) {
            return this.relative.get(orientation);
        }

        public Color getColor() {
            return this.color;
        }
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
