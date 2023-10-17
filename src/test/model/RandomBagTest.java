package model;

import model.tetromino.*;
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
            LinkedList<AbstractTetromino> allPieces = new LinkedList<AbstractTetromino>(
                    List.of(new I(), new J(), new L(), new O(), new S(), new T(), new Z()));

            for (int j = 0; j < 7; j++) {
                AbstractTetromino tetromino = this.bag.pop();

                ListIterator<AbstractTetromino> iterator = allPieces.listIterator();

                while (iterator.hasNext()) {
                    AbstractTetromino next = iterator.next();
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
        List<AbstractTetromino> actual = this.bag.getPreview();
        List<AbstractTetromino> expected = new LinkedList<AbstractTetromino>();

        for (int i = 0; i < 4; i++) {
            expected.add(this.bag.pop());
        }

        assertEquals(expected, actual);
    }
}
