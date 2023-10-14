package model;

import model.tetromino.AbstractTetromino;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Playfield {
    private final int[][] matrix;
    private final Map<Integer, AbstractTetromino> tetrominoMap;

    private AbstractTetromino current;

    private boolean readyToLock;

    protected Playfield() {
        this.matrix = new int[22][10];
        this.tetrominoMap = new HashMap<Integer, AbstractTetromino>();
    }

    protected boolean spawn(AbstractTetromino tetromino) {
        for (int c : tetromino.occupies()) {
            int[] coords = AbstractTetromino.coords(c);
            if (this.matrix[coords[1]][coords[0]] != 0) {
                return false;
            }
        }

        this.current = tetromino;
        return true;
    }

    protected AbstractTetromino getCurrent() {
        return this.current;
    }

    protected AbstractTetromino swapCurrent(AbstractTetromino tetromino) {
        AbstractTetromino c = this.current;

        if (tetromino == null) {
            throw new IllegalArgumentException();
        }

        this.current = tetromino;
        return c;
    }

    protected boolean move(AbstractTetromino.Direction direction) {
        if (this.current == null) {
            return false;
        }

        for (int c : this.current.testMove(direction)) {
            int[] coords = AbstractTetromino.coords(c);

            if (coords[1] < 0) {
                this.readyToLock = true;
                return false;
            }

            if (coords[0] < 0 || coords[0] > 9) {
                return false;
            }

            if (this.matrix[coords[1]][coords[0]] != 0) {
                if (direction == AbstractTetromino.Direction.DOWN) {
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
        Set<Integer> occupiedCoords;
        OUTER: while ((occupiedCoords = this.current.testRotate(direction)) != null) {
            for (int c : occupiedCoords) {
                int[] oc = AbstractTetromino.coords(c);

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
            int[] coords = AbstractTetromino.coords(c);
            this.matrix[coords[1]][coords[0]] = this.current.getId();
        }

        this.tetrominoMap.put(this.current.getId(), this.current);

        this.current = null;
        this.readyToLock = false;

        this.clear();

    }

    protected boolean isReadyToLock() {
        return this.readyToLock;
    }

    protected int get(int x, int y) {
        if (this.matrix[y][x] != 0) {
            return this.matrix[y][x];
        }

        if (this.current != null && this.current.occupies(x, y)) {
            return this.current.getId();
        }

        return 0;
    }

    protected AbstractTetromino getTetromino(int x, int y) {
        return this.tetrominoMap.get(this.get(x, y));
    }

    protected int[][] getMatrix() {
        return this.matrix;
    }

    private void clear() {
        OUTER: for (int i = 0; i < this.matrix.length; i++) {
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
