package model;

import java.util.*;

public class RandomBag {
    private final Queue<Tetromino> bag;

    public RandomBag() {
        this.bag = new ArrayDeque<Tetromino>(14);
        this.fillBag();
        this.fillBag();
    }

    public RandomBag(Queue<Tetromino> queue) {
        if (queue.size() <= 7) {
            throw new IllegalArgumentException("The queue must have at least 7 tetrominoes.");
        }

        this.bag = queue;
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
