package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RandomBagTest {
    RandomBag bag;

    @BeforeEach
    void runBefore() {
        this.bag = new RandomBag();
    }

    @Test
    void testPop() {
        for (int i = 0; i < 1000; i++) {
            LinkedList<Tetromino> allPieces = new LinkedList<Tetromino>();

            for (Tetromino.Type t : Tetromino.Type.values()) {
                allPieces.add(new Tetromino(t));
            }

            for (int j = 0; j < 7; j++) {
                Tetromino tetromino = this.bag.pop();

                ListIterator<Tetromino> iterator = allPieces.listIterator();

                while (iterator.hasNext()) {
                    Tetromino next = iterator.next();
                    if (next.getClass().isInstance(tetromino)) {
                        iterator.remove();
                        break;
                    }
                }
            }

            assertTrue(allPieces.isEmpty());
        }
    }

    @Test
    void testGetPreview() {
        List<Tetromino> actual = this.bag.getPreview();
        List<Tetromino> expected = new LinkedList<Tetromino>();

        for (int i = 0; i < 5; i++) {
            expected.add(this.bag.pop());
        }

        assertEquals(expected, actual);
    }
}
