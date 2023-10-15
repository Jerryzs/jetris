package model;

import model.tetromino.AbstractTetromino;
import model.tetromino.T;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameTest {
    Game game;
    Playfield playfield;
    RandomBag randomBag;
    AbstractTetromino tetromino;

    @BeforeEach
    void runBefore() {
        this.playfield = new Playfield();
        this.randomBag = new RandomBag();
        this.tetromino = new T();

        this.game = new Game(null, 1, this.playfield, this.randomBag, this.tetromino);

        this.game.toggleGame();
    }

    @Test
    void testMovements() {
        this.game.toggleGame();
        while (this.tetromino.isHidden()) {
            this.game.run();
        }
        this.game.toggleGame();

        assertEquals(AbstractTetromino.num(4, 19), AbstractTetromino.num(this.tetromino.getCoords()));

        this.game.moveLeft();
        assertEquals(AbstractTetromino.num(3, 19), AbstractTetromino.num(this.tetromino.getCoords()));

        this.game.moveRight();
        assertEquals(AbstractTetromino.num(4, 19), AbstractTetromino.num(this.tetromino.getCoords()));
    }
}
