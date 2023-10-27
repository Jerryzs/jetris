package persistence;

import model.Game;
import model.Playfield;
import model.RandomBag;
import model.tetromino.AbstractTetromino;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

public class Save {
    private static final String DEFAULT_PATH = "./data/save.json";

    private final File file;

    private Game game;

    public Save() {
        this(Save.DEFAULT_PATH);
    }

    public Save(String path) {
        this(null, path);
    }

    public Save(Game game) {
        this(game, Save.DEFAULT_PATH);
    }

    public Save(Game game, String path) {
        this.file = new File(path);
        this.game = game;
    }

    public Game getGame() {
        return this.game;
    }

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

    public Game load() {
        try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
            StringBuilder json = new StringBuilder();

            int r;
            while ((r = reader.read()) != -1) {
                json.append((char) r);
            }

            JSONObject obj = new JSONObject(json.toString());

            this.game = new Game(
                    60,
                    new Playfield(Save.getMatrix(obj.getJSONArray("matrix"))),
                    new RandomBag(Save.getRandomBag(obj.getJSONArray("bag"))),
                    obj.has("current") ? Save.getTetromino(obj.getJSONObject("current")) : null,
                    obj.has("hold") ? Save.getTetromino(obj.getJSONObject("hold")) : null,
                    obj.getBoolean("holdingAllowed")
            );
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }

        return this.game;
    }

    private JSONArray getMatrixJson() {
        JSONArray array = new JSONArray();

        for (int[] row : this.game.getPlayfield().getMatrix()) {
            int bin = 0;
            for (int i = 0; i < row.length; i++) {
                bin += row[i] == 0 ? 0 : 1 << (i * 3);
            }
            array.put(bin);
        }

        return array;
    }

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

    private JSONArray getRandomBagJson() {
        JSONArray array = new JSONArray();

        Iterator<AbstractTetromino> iterator = this.game.getBagIterator();
        while (iterator.hasNext()) {
            array.put(iterator.next().getClass().getName());
        }

        return array;
    }

    private static Queue<AbstractTetromino> getRandomBag(JSONArray array) throws IOException {
        Queue<AbstractTetromino> queue = new ArrayDeque<AbstractTetromino>(14);

        try {
            for (int i = 0; i < array.length(); i++) {
                queue.offer((AbstractTetromino) Class.forName(array.getString(i)).getConstructor().newInstance());
            }
        } catch (ClassNotFoundException e) {
            throw new IOException();
        } catch (NoSuchMethodException
                 | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException e) {
            throw new AssertionError(e);
        }

        return queue;
    }

    private static JSONObject getTetrominoJson(AbstractTetromino tetromino) {
        if (tetromino == null) {
            return null;
        }

        return new JSONObject()
                .put("type", tetromino.getClass().getName())
                .put("coords", tetromino.numCoords())
                .put("orientation", tetromino.getOrientation().name())
                .put("test", tetromino.getTest());
    }

    private static AbstractTetromino getTetromino(JSONObject object) throws IOException {
        try {
            return (AbstractTetromino) Class.forName(object.getString("type"))
                    .getConstructor(int[].class, AbstractTetromino.Direction.class, int.class)
                    .newInstance(
                            AbstractTetromino.coords(object.getInt("coords")),
                            AbstractTetromino.Direction.valueOf(object.getString("orientation")),
                            object.getInt("test"));
        } catch (ClassNotFoundException e) {
            throw new IOException();
        } catch (NoSuchMethodException
                 | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }
}
