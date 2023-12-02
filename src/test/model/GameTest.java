package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {
    Game defaultGame;

    Game game;
    Playfield playfield;
    RandomBag randomBag;
    Score score;
    Tetromino tetromino;

    int drawCount;

    @BeforeEach
    void runBefore() {
        this.defaultGame = new Game(30);
        this.defaultGame.toggleGame();

        this.playfield = new Playfield();
        this.randomBag = new RandomBag();
        this.score = new Score(this.playfield);
        this.tetromino = new Tetromino(Tetromino.Type.T);

        this.game = new Game(10, this.playfield, this.randomBag, this.score, this.tetromino, null, true);
        this.game.toggleGame();
    }

    @Test
    void testToggle() {
        this.game.toggleGame();
        assertFalse(this.game.isPaused());

        this.game.toggleGame();
        assertTrue(this.game.isPaused());
    }

    @Test
    void testGetPreview() {
        List<Tetromino> previewWithCurrent = new LinkedList<Tetromino>();

        previewWithCurrent.add(this.tetromino);
        previewWithCurrent.addAll(this.randomBag.getPreview().subList(0, 4));

        assertEquals(previewWithCurrent, this.game.getPreview());

        this.game.toggleGame();
        while (this.tetromino.isHidden()) {
            this.game.run();
        }
        this.game.toggleGame();

        assertEquals(this.randomBag.getPreview(), this.game.getPreview());
    }

    @Test
    void testSoftDrop() {
        this.game.toggleGame();
        while (this.tetromino.isHidden()) {
            this.game.run();
        }
        this.game.toggleGame();

        assertEquals(19, this.tetromino.getCoords()[1]);

        this.game.softDrop();

        this.game.toggleGame();
        this.game.run();
        this.game.toggleGame();

        assertEquals(18, this.tetromino.getCoords()[1]);
    }

    @Test
    void testHardDrop() {
        this.game.toggleGame();
        while (this.tetromino.isHidden()) {
            this.game.run();
        }
        this.game.toggleGame();

        assertEquals(this.tetromino, this.playfield.getCurrent());

        Tetromino next = this.randomBag.getPreview().get(0);
        this.game.hardDrop();

        assertEquals(next, this.playfield.getCurrent());
        assertEquals(this.tetromino.getType().ordinal() + 1, this.game.get(4, 1));
    }

    @Test
    void testCounterReset() {
        this.game.toggleGame();
        while (this.tetromino.isHidden()) {
            this.game.run();
        }

        assertEquals(19, this.tetromino.getCoords()[1]);
        this.game.run();

        for (int i = 0; i < 8; i++) {
            assertEquals(19, this.tetromino.getCoords()[1]);
            this.game.rotateRight();
            for (int j = 0; j < 9; j++) {
                this.game.run();
            }
        }

        this.game.run();
        assertEquals(18, this.tetromino.getCoords()[1]);

        do {
            this.game.run();
        } while (!this.tetromino.occupies(4, 0));

        this.game.run();

        assertEquals(this.tetromino, this.playfield.getCurrent());

        for (int i = 0; i < 4; i++) {
            this.game.moveRight();
            for (int j = 0; j < 9; j++) {
                this.game.run();
            }
            this.game.moveLeft();
            for (int j = 0; j < 9; j++) {
                this.game.run();
            }
        }

        this.game.run();

        assertNotEquals(this.tetromino, this.playfield.getCurrent());
    }

    @Test
    void testMovements() {
        this.game.toggleGame();
        while (this.tetromino.isHidden()) {
            this.game.run();
        }
        this.game.toggleGame();

        assertEquals(this.tetromino.getType().ordinal() + 1, this.game.get(4, 19));

        this.game.moveLeft();
        assertEquals(this.tetromino.getType().ordinal() + 1, this.game.get(3, 20));

        this.game.moveRight();
        assertEquals(this.tetromino.getType().ordinal() + 1, this.game.get(4, 20));
    }

    @Test
    void testRotation() {
        this.game.toggleGame();
        while (this.tetromino.isHidden()) {
            this.game.run();
        }
        this.game.toggleGame();

        assertEquals(this.tetromino.getOrientation(), Tetromino.Direction.DOWN);

        this.game.rotateLeft();

        assertEquals(this.tetromino.getOrientation(), Tetromino.Direction.RIGHT);
        assertEquals(0, this.game.get(5, 19));

        this.game.rotateRight();

        assertEquals(this.tetromino.getOrientation(), Tetromino.Direction.DOWN);
        assertEquals(0, this.game.get(4, 18));

        this.game.rotateRight();

        assertEquals(this.tetromino.getOrientation(), Tetromino.Direction.LEFT);
        assertEquals(0, this.game.get(3, 19));

        this.game.rotateLeft();

        assertEquals(this.tetromino.getOrientation(), Tetromino.Direction.DOWN);
        assertEquals(0, this.game.get(4, 18));
    }

    @Test
    void testHold() {
        assertFalse(this.game.hold());

        this.game.toggleGame();
        while (this.tetromino.isHidden()) {
            this.game.run();
        }
        this.game.toggleGame();

        assertTrue(this.game.hold());

        assertEquals(this.tetromino, this.game.getHold());
        assertEquals(4, this.game.getHold().getCoords()[0]);
        assertEquals(18, this.game.getHold().getCoords()[1]);
        assertEquals(Tetromino.Direction.DOWN, this.game.getHold().getOrientation());

        assertFalse(this.game.hold());

        this.game.toggleGame();
        while (this.tetromino.isHidden()) {
            this.game.run();
        }
        this.game.toggleGame();

        assertFalse(this.game.hold());

        Tetromino current = this.playfield.getCurrent();

        this.game.toggleGame();
        while (this.playfield.getCurrent() == current || this.playfield.getCurrent().isHidden()) {
            this.game.run();
        }
        this.game.toggleGame();

        assertTrue(this.game.hold());
    }

    @Test
    void testGameOver() {
        assertFalse(this.game.isOver());

        this.game.toggleGame();
        for (int i = 0; i < 100000; i++) {
            this.game.run();
        }
        this.game.toggleGame();

        assertTrue(this.game.isOver());
    }
}
