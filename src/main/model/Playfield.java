package model;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class Playfield {
    private final int[][] matrix;

    private Tetromino current;

    private boolean readyToLock;

    public Playfield() {
        this(new int[22][10]);
    }

    private Playfield(int[][] matrix) {
        if (matrix == null || matrix.length != 22 || Arrays.stream(matrix).anyMatch((row) -> row.length != 10)) {
            throw new IllegalArgumentException("Matrix must not-null and rectangular.");
        }

        this.matrix = matrix;
    }

    /**
     * REQUIRES: this.game != null
     * <p>
     * EFFECTS: Create a JSON array from the playfield matrix of the current
     * game.
     *
     * @return The JSON array representing the matrix
     */
    public static JSONArray toJsonArray(Playfield playfield) {
        JSONArray array = new JSONArray();

        for (int[] row : playfield.getMatrix()) {
            int bin = 0;
            for (int i = 0; i < row.length; i++) {
                bin += row[i] == 0 ? 0 : row[i] << (i * 3);
            }
            array.put(bin);
        }

        return array;
    }

    /**
     * REQUIRES: array != null and array.length() == 22 and all array elements
     * are integers
     * <p>
     * EFFECTS: Recover the playfield matrix from the JSON array in the save
     * file into a 2-dimensional array that can be used to reconstruct a
     * playfield object.
     *
     * @param array The JSON array representing the matrix saved to the file
     * @return The 2-d integer array representing the playfield matrix
     * @throws IOException If the JSON array is unreadable or if its content is
     *                     invalid
     */
    public static Playfield fromJsonArray(JSONArray array) throws IOException {
        int[][] matrix = new int[22][10];

        try {
            for (int i = 0; i < array.length(); i++) {
                int row = array.getInt(i);

                for (int j = 0; j < matrix[i].length; j++) {
                    matrix[i][j] = row % 8;
                    row /= 8;
                }
            }
        } catch (JSONException | ArrayIndexOutOfBoundsException e) {
            throw new IOException();
        }

        return new Playfield(matrix);
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

    public boolean isEmpty() {
        for (int i = 0; i < this.matrix[0].length; i++) {
            if (this.matrix[0][i] != 0) {
                return false;
            }
        }

        return true;
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

            this.readyToLock = false;

            this.current.rotate();
            return true;
        }

        return false;
    }

    protected int lockdown() {
        for (int c : this.current.occupies()) {
            int[] coords = Tetromino.coords(c);
            this.matrix[coords[1]][coords[0]] = this.current.getType().ordinal() + 1;
        }

        this.current = null;
        this.readyToLock = false;

        return this.clear();
    }

    protected boolean isReadyToLock() {
        return this.readyToLock;
    }

    private int clear() {
        int count = 0;
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
            count++;
            i--;
        }

        return count;
    }
}
