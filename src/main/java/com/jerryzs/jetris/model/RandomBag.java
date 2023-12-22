package com.jerryzs.jetris.model;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.*;

public class RandomBag {
    private final Queue<Tetromino> bag;

    public RandomBag() {
        this.bag = new ArrayDeque<Tetromino>(14);
        this.fillBag();
        this.fillBag();
    }

    private RandomBag(Queue<Tetromino> queue) {
        if (queue.size() <= 7) {
            throw new IllegalArgumentException("The queue must have at least 7 tetrominoes.");
        }

        this.bag = queue;
    }

    /**
     * REQUIRES: this.game != null
     * <p>
     * EFFECTS: Create a JSON array from the content of the 7-bag of the current
     * game.
     *
     * @return The JSON array representing the 7-bag
     */
    public static JSONArray toJsonArray(RandomBag bag) {
        JSONArray array = new JSONArray();

        Iterator<Tetromino> iterator = bag.getIterator();
        while (iterator.hasNext()) {
            array.put(iterator.next().getType().name());
        }

        return array;
    }

    /**
     * REQUIRES: array != null and array.length() > 7
     * <p>
     * EFFECTS: Recover the content of the 7-bag from the save file as a
     * RandomBag object.
     *
     * @param array The JSON array representing the 7-bag saved to the file
     * @return The RandomBag object
     * @throws IOException If the JSON array is unreadable or if its content is
     *                     invalid
     */
    public static RandomBag fromJsonArray(JSONArray array) throws IOException {
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

        return new RandomBag(queue);
    }

    public Tetromino pop() {
        Tetromino t = this.bag.poll();

        if (this.bag.size() <= 7) {
            this.fillBag();
        }

        return t;
    }

    public List<Tetromino> getPreview() {
        return new LinkedList<Tetromino>(this.bag).subList(0, 5);
    }

    public Iterator<Tetromino> getIterator() {
        return this.bag.iterator();
    }

    private void fillBag() {
        assert this.bag.size() <= 7;

        ArrayList<Tetromino> b = new ArrayList<Tetromino>();

        for (Tetromino.Type t : Tetromino.Type.values()) {
            b.add(new Tetromino(t));
        }

        Collections.shuffle(b);
        this.bag.addAll(b);
    }
}
