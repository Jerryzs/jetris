package com.jerryzs.jetris.persistence;

import com.jerryzs.jetris.model.Game;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

/**
 * A tool that facilitates the saving and recovering the complete state of a
 * Jetris game to and from a JSON file.
 */
public class Save {
    private static final String DEFAULT_PATH = "./save.json";

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

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.file))) {
            writer.write(Game.toJson(this.game).toString());
            return true;
        } catch (IOException e) {
            return false;
        }
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

            return this.game = Game.fromJson(new JSONObject(json.toString()), framerate);
        } catch (IOException | JSONException e) {
            return null;
        }
    }
}
