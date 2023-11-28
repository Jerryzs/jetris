package persistence;

import model.Game;
import model.Playfield;
import model.RandomBag;
import model.Tetromino;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

/**
 * A tool that facilitates the saving and recovering the complete state of a
 * Jetris game to and from a JSON file.
 */
public class Save {
    private static final String DEFAULT_PATH = "./data/save.json";

    private final File file;

    private Game game;

    /**
     * EFFECTS: Initialize a Save object with the default path and no game
     * object. This means that the save() method cannot be called unless the
     * load() method has been called and a game object has been successfully
     * loaded first.
     */
    public Save() {
        this(Save.DEFAULT_PATH);
    }

    /**
     * EFFECTS: Initialize a Save object using a specified path and no game
     * object. This means that the save() method cannot be called unless the
     * load() method has been called and a game object has been successfully
     * loaded first.
     *
     * @param path The path to load from and save to
     */
    public Save(String path) {
        this(null, path);
    }

    /**
     * EFFECTS: Initialize a Save object with the default path and a specified
     * game object.
     *
     * @param game The game object to save
     */
    public Save(Game game) {
        this(game, Save.DEFAULT_PATH);
    }

    /**
     * EFFECTS: Initialize a Save object with a specified path and a specified
     * game object.
     *
     * @param game The game object to save
     * @param path The path to load from and save to
     */
    public Save(Game game, String path) {
        this.file = new File(path);
        this.game = game;
    }

    public Game getGame() {
        return this.game;
    }

    /**
     * EFFECTS: Write the current game state to the save file.
     *
     * @return True if the game state is successfully saved to the file;
     * otherwise, false
     */
    public boolean store() {
        if (this.game == null) {
            throw new IllegalStateException("No game has been loaded.");
        }

        JSONObject obj = new JSONObject();

        obj.put("current", Save.getTetrominoJson(this.game.getPlayfield().getCurrent()));
        obj.put("hold", Save.getTetrominoJson(this.game.getHold()));
        obj.put("holdingAllowed", this.game.getHoldingAllowed());
        obj.put("bag", this.getRandomBagJson());
        obj.put("gravity", this.game.getGravity());
        obj.put("matrix", this.getMatrixJson());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file))) {
            writer.write(obj.toString());
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * EFFECTS: Read the save file and recover the saved game state.
     *
     * @return The game object recovered from the save file or null, if the file
     * does not exist or the save data is incorrect
     */
    public Game load(int framerate) {
        try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
            StringBuilder json = new StringBuilder();

            int r;
            while ((r = reader.read()) != -1) {
                json.append((char) r);
            }

            JSONObject obj = new JSONObject(json.toString());

            this.game = new Game(
                    framerate,
                    new Playfield(Save.getMatrix(obj.getJSONArray("matrix"))),
                    new RandomBag(Save.getRandomBag(obj.getJSONArray("bag"))),
                    obj.has("current") ? Save.getTetromino(obj.getJSONObject("current")) : null,
                    obj.has("hold") ? Save.getTetromino(obj.getJSONObject("hold")) : null,
                    obj.getBoolean("holdingAllowed")
            );
        } catch (IOException | JSONException e) {
            return null;
        }

        return this.game;
    }

    /**
     * REQUIRES: this.game != null
     * <p>
     * EFFECTS: Create a JSON array from the playfield matrix of the current
     * game.
     *
     * @return The JSON array representing the matrix
     */
    private JSONArray getMatrixJson() {
        JSONArray array = new JSONArray();

        for (int[] row : this.game.getPlayfield().getMatrix()) {
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
    private static int[][] getMatrix(JSONArray array) throws IOException {
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

        return matrix;
    }

    /**
     * REQUIRES: this.game != null
     * <p>
     * EFFECTS: Create a JSON array from the content of the 7-bag of the current
     * game.
     *
     * @return The JSON array representing the 7-bag
     */
    private JSONArray getRandomBagJson() {
        JSONArray array = new JSONArray();

        Iterator<Tetromino> iterator = this.game.getBagIterator();
        while (iterator.hasNext()) {
            array.put(iterator.next().getType().name());
        }

        return array;
    }

    /**
     * REQUIRES: array != null and array.length() > 7
     * <p>
     * EFFECTS: Recover the content of the 7-bag from the save file as a queue
     * that can be used to reconstruct a 7-bag object.
     *
     * @param array The JSON array representing the 7-bag saved to the file
     * @return The queue representing the 7-bag
     * @throws IOException If the JSON array is unreadable or if its content is
     *                     invalid
     */
    private static Queue<Tetromino> getRandomBag(JSONArray array) throws IOException {
        Queue<Tetromino> queue = new ArrayDeque<Tetromino>(14);

        try {
            for (int i = 0; i < array.length(); i++) {
                queue.offer(new Tetromino(Tetromino.Type.valueOf(array.getString(i))));
            }
        } catch (JSONException e) {
            throw new IOException();
        } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
        }

        return queue;
    }

    /**
     * EFFECTS: Convert the state of a tetromino object into a JSON object.
     *
     * @param tetromino The tetromino to convert
     * @return The JSON object representing the tetromino
     */
    private static JSONObject getTetrominoJson(Tetromino tetromino) {
        if (tetromino == null) {
            return null;
        }

        return new JSONObject()
                .put("type", tetromino.getType().name())
                .put("coords", tetromino.numCoords())
                .put("orientation", tetromino.getOrientation().name());
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
    private static Tetromino getTetromino(JSONObject object) throws IOException {
        try {
            return new Tetromino(
                    Tetromino.Type.valueOf(object.getString("type")),
                    Tetromino.coords(object.getInt("coords")),
                    Tetromino.Direction.valueOf(object.getString("orientation")));
        } catch (JSONException e) {
            throw new IOException();
        } catch (IllegalArgumentException e) {
            throw new AssertionError();
        }
    }
}
