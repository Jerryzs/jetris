package model;

import model.tetromino.*;

import java.util.*;

public class RandomBag {
    private final Queue<AbstractTetromino> bag;

    public RandomBag() {
        this.bag = new ArrayDeque<AbstractTetromino>(14);
        this.fillBag();
        this.fillBag();
    }

    public AbstractTetromino pop() {
        AbstractTetromino t = this.bag.poll();

        if (this.bag.size() <= 7) {
            this.fillBag();
        }

        return t;
    }

    public List<AbstractTetromino> getPreview() {
        return new LinkedList<AbstractTetromino>(this.bag).subList(0, 4);
    }

    private void fillBag() {
        if (this.bag.size() > 7) {
            throw new IllegalStateException();
        }

        ArrayList<AbstractTetromino> b = new ArrayList<AbstractTetromino>(List.of(
                new I(), new J(), new L(), new O(), new S(), new T(), new Z()
        ));

        Collections.shuffle(b);
        this.bag.addAll(b);
    }
}
