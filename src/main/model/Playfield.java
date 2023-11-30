package model;

import java.util.Arrays;
import java.util.Set;

public class Playfield {
    private final int[][] matrix;

    private Tetromino current;

    private boolean readyToLock;

    public Playfield() {
        this(new int[22][10]);
    }

    public Playfield(int[][] matrix) {
        if (matrix == null || matrix.length != 22 || Arrays.stream(matrix).anyMatch((row) -> row.length != 10)) {
            throw new IllegalArgumentException("Matrix must not-null and rectangular.");
        }

        this.matrix = matrix;
    }

    protected int get(int x, int y) {
        if (this.matrix[y][x] != 0) {
            return this.matrix[y][x];
        }

        if (this.current != null && this.current.occupies(x, y)) {
            return this.current.getType().ordinal() + 1;
        }

        return 0;
    }

    public int[][] getMatrix() {
        int[][] matrix = new int[22][10];

        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(this.matrix[i], 0, matrix[i], 0, matrix[i].length);
        }

        return matrix;
    }

    protected boolean spawn(Tetromino tetromino) {
        for (int c : tetromino.occupies()) {
            int[] coords = Tetromino.coords(c);
            if (this.matrix[coords[1]][coords[0]] != 0) {
                return false;
            }
        }

        this.current = tetromino;
        return true;
    }

    public Tetromino getCurrent() {
        return this.current;
    }

    protected Tetromino swapCurrent(Tetromino tetromino) {
        this.readyToLock = false;
        Tetromino c = this.current;

        if (tetromino == null) {
            throw new IllegalArgumentException();
        }

        this.current = tetromino;
        return c;
    }

    protected boolean move(Tetromino.Direction direction) {
        if (this.current == null || (this.current.isHidden() && direction != Tetromino.Direction.DOWN)) {
            return false;
        }

        for (int c : this.current.testMove(direction)) {
            int[] coords = Tetromino.coords(c);

            if (coords[1] < 0) {
                this.readyToLock = true;
                return false;
            }

            if (coords[0] < 0 || coords[0] > 9) {
                return false;
            }

            if (this.matrix[coords[1]][coords[0]] != 0) {
                if (direction == Tetromino.Direction.DOWN) {
                    this.readyToLock = true;
                }
                return false;
            }
        }

        this.readyToLock = false;

        this.current.move(direction);
        return true;
    }

    protected boolean rotate(int direction) {
        if (Math.abs(direction) != 1) {
            throw new IllegalArgumentException();
        }

        if (this.current == null || this.current.isHidden()) {
            return false;
        }

        Set<Integer> occupiedCoords;
        OUTER:
        while ((occupiedCoords = this.current.testRotate(direction)) != null) {
            for (int c : occupiedCoords) {
                int[] oc = Tetromino.coords(c);

                if (oc[1] < 0 || oc[0] < 0 || oc[0] > 9 || this.matrix[oc[1]][oc[0]] != 0) {
                    continue OUTER;
                }
            }

            this.current.rotate();
            return true;
        }

        return false;
    }

    protected void lockdown() {
        for (int c : this.current.occupies()) {
            int[] coords = Tetromino.coords(c);
            this.matrix[coords[1]][coords[0]] = this.current.getType().ordinal() + 1;
        }

        this.current = null;
        this.readyToLock = false;

        this.clear();

    }

    protected boolean isReadyToLock() {
        return this.readyToLock;
    }

    private void clear() {
        OUTER:
        for (int i = 0; i < this.matrix.length; i++) {
            for (int j = 0; j < this.matrix[i].length; j++) {
                if (this.matrix[i][j] == 0) {
                    continue OUTER;
                }
            }

            for (int j = i + 1; j < this.matrix.length - 2; j++) {
                this.matrix[j - 1] = this.matrix[j];
            }

            this.matrix[this.matrix.length - 2 - 1] = new int[10];

            i--;
        }
    }
}
