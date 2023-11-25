package ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import model.tetromino.AbstractTetromino;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The interface between an underlying Jetris game and a terminal environment.
 * This class configures a terminal screen and updates it with the box drawings
 * of menu options or the latest state of the Jetris game. It reads user inputs
 * from the InputStream and outputs the drawings to the OutputStream provided
 * for the target terminal environment at a specified number of times per
 * second. It is designed to essentially handle all interactions with the user
 * and invoke the appropriate actions from the user's instructions.
 */
// suppress warnings on necessary workarounds for ascii auto-test programs
@SuppressWarnings({"AvoidEscapedUnicodeCharacters", "UnnecessaryUnicodeEscape", "checkstyle:SuppressWarnings"})
public class CLI extends UserInterface {
    private final TerminalScreen screen;
    private final TextGraphics textGraphics;

    private TerminalSize terminalSize;
    private int scale;

    /**
     * REQUIRES: in != null and out != null and refreshRate > 0
     * <p>
     * EFFECTS: Set up the Lanterna terminal screen and create a new game
     * session.
     *
     * @param in        InputStream of the terminal
     * @param out       OutputStream of the terminal
     * @param framerate The number of times the game (and screen) is updated per
     *                  second
     */
    public CLI(InputStream in, OutputStream out, int framerate) {
        super(framerate);

        try {
            this.screen = new DefaultTerminalFactory(out, in, StandardCharsets.UTF_8).createScreen();
            this.screen.startScreen();
            this.screen.setCursorPosition(null);
            this.textGraphics = this.screen.newTextGraphics();
            this.scale = Math.max(this.screen.getTerminalSize().getRows() / 22, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.mainMenu();
        this.startTimer();
    }

    @Override
    protected CLMenu getMenu() {
        return (CLMenu) super.getMenu();
    }

    @Override
    protected CLMenu getMainMenu() {
        return new CLMenu("Jetris")
                .add("Start", this::start)
                .add("Load save", this::load)
                .add("Exit", () -> System.exit(0));
    }

    @Override
    protected CLMenu getPauseMenu() {
        return new CLMenu("Paused")
                .add("Resume", this::resume)
                .add("Save", this::save)
                .add("Main menu", this::mainMenu)
                .add("Exit", () -> System.exit(0));
    }

    /**
     * @throws IOException Error propagated from the underlying stream.
     */
    private void checkInput() throws IOException {
        KeyStroke key = this.screen.pollInput();

        if (key != null) {
            if (key.getKeyType() == KeyType.EOF) {
                System.exit(0);
            }

            if (key.getKeyType() == KeyType.Character) {
                if (key.getCharacter() == '=' && this.scale < 5) {
                    this.scale++;
                } else if (key.getCharacter() == '-' && this.scale > 1) {
                    this.scale--;
                }
            }

            if (this.getMenu() != null) {
                this.checkMenuInput(key);
            } else {
                this.checkGameInput(key);
            }
        }
    }

    private void checkMenuInput(KeyStroke key) {
        if (key.getKeyType() == KeyType.Escape) {
            this.resume();
        }

        if (key.getKeyType() == KeyType.Enter) {
            this.getMenu().trigger();
        }

        if (key.getKeyType() == KeyType.Character) {
            if (key.getCharacter() == 'w') {
                this.getMenu().prev();
            } else if (key.getCharacter() == 's') {
                this.getMenu().next();
            } else if (key.getCharacter() == ' ') {
                this.getMenu().trigger();
            }
        }
    }

    /**
     * REQUIRES: this.screen != null and this.game != null
     * <p>
     * MODIFIES: this
     * <p>
     * EFFECTS: Check for any user input in the buffer from Lanterna and perform
     * any appropriate actions with respect to the input.
     */
    private void checkGameInput(KeyStroke key) {
        if (key.getKeyType() == KeyType.Escape) {
            this.pause();
        }

        if (key.getKeyType() == KeyType.Character) {
            this.handleInput(Character.toLowerCase(key.getCharacter()));
        }
    }

    /**
     * REQUIRES: this.game != null
     * <p>
     * MODIFIES" this.game
     * <p>
     * EFFECTS" Perform the appropriate action for a keyboard character (ideally
     * retrieved as a user input).
     *
     * @param key The keyboard character.
     */
    private void handleInput(char key) {
        if (key == ' ') {
            this.game.hold();
        } else if (key == 'w') {
            this.game.hardDrop();
        } else if (key == 'a') {
            this.game.moveLeft();
        } else if (key == 's') {
            this.game.softDrop();
        } else if (key == 'd') {
            this.game.moveRight();
        } else if (key == 'q' || key == 'k') {
            this.game.rotateLeft();
        } else if (key == 'e' || key == 'l') {
            this.game.rotateRight();
        }
    }

    /**
     * REQUIRES: this.screen != null
     * <p>
     * MODIFIES: this
     * <p>
     * EFFECTS: Check if the terminal has been resized and update this with the
     * new terminal size.
     */
    private void updateTerminalSize() {
        TerminalSize newSize = this.screen.doResizeIfNecessary();
        if (newSize != null) {
            this.terminalSize = newSize;
        }
    }

    /**
     * REQUIRES: width >= 0
     * <p>
     * EFFECTS: Calculate the column (x coordinate) to start drawing for the
     * game to be centered in the terminal.
     *
     * @param width The width of the terminal screen
     * @return The column to start drawing from
     */
    private int getCenterLeftLimit(int width) {
        if (this.terminalSize == null || this.terminalSize.getColumns() < width) {
            return 0;
        }

        return this.terminalSize.getColumns() / 2 - width / 2;
    }

    /**
     * REQUIRES: sb != null
     * <p>
     * MODIFIES: sb
     * <p>
     * EFFECTS: Append a scaled square block with box drawing unicode characters
     * to the StringBuilder if the condition is true; otherwise, append an empty
     * space the size of a scaled square block.
     *
     * @param sb        The StringBuilder object to append to
     * @param condition Whether to append a solid block or an empty space
     */
    private void appendHorizontalBlock(StringBuilder sb, boolean condition) {
        sb.append((condition ? "\u2588\u2588" : "  ").repeat(this.scale));
    }

    /**
     * REQUIRES: sb != null and line in {0, 1} and tetromino != null
     * <p>
     * MODIFIES: sb
     * <p>
     * EFFECTS: Append the specific line of the default state of a tetromino to
     * the StringBuilder.
     *
     * @param sb        The StringBuilder object to append to
     * @param line      The line of the tetromino to append. Since the maximum
     *                  number of lines occupied by the default state of any
     *                  tetromino is 2, this argument can only accept 0 or 1.
     * @param tetromino The tetromino whose default state is to be used
     */
    private void appendStandaloneTetromino(StringBuilder sb, int line, AbstractTetromino tetromino) {
        int l = line / this.scale;
        for (int k = 0; k < 4; k++) {
            this.appendHorizontalBlock(sb, tetromino.getStandalone()[4 * l + k] != 0);
        }
    }

    /**
     * REQUIRES: sb != null and line >= 0 and this.game != null
     * <p>
     * MODIFIES: sb
     * <p>
     * EFFECTS: Append the appropriate texts or drawings for the left margin in
     * the specified line to the StringBuilder.
     *
     * @param sb   The StringBuilder to append to
     * @param line The line which the left margin is for
     */
    private void appendLeftPadding(StringBuilder sb, int line) {
        if (line < this.scale) {
            sb.append(" ".repeat(this.scale * 2))
                    .append(CLI.HOLD_TEXT[this.scale - 1][line])
                    .append(" ".repeat(this.scale * 2));
        } else if (line < 2 * this.scale || line >= 4 * this.scale || this.game.getHold() == null) {
            sb.append(" ".repeat(12 * this.scale));
        } else {
            sb.append(" ".repeat(this.scale * 2));
            this.appendStandaloneTetromino(sb, line - 2 * this.scale, this.game.getHold());
            sb.append(" ".repeat(this.scale * 2));
        }

        sb.append("\u2502");
    }

    /**
     * REQUIRES: line >= 0 and this.game != null
     * <p>
     * MODIFIES: sb
     * <p>
     * EFFECTS: Append the appropriate texts or drawings for the right margin in
     * the specified line to the StringBuilder.
     *
     * @param sb   The StringBuilder to append to
     * @param line The line which the right margin is for
     */
    private void appendRightPadding(StringBuilder sb, int line) {
        sb.append("\u2502");

        if (line < this.scale) {
            sb.append(" ".repeat(this.scale * 2))
                    .append(CLI.NEXT_TEXT[this.scale - 1][line])
                    .append(" ".repeat(this.scale * 2));
        } else if (line < 2 * this.scale
                || (line - 2 * this.scale) % (3 * this.scale) >= 2 * this.scale
                || line > 16 * this.scale) {
            sb.append(" ".repeat(12 * this.scale));
        } else {
            List<AbstractTetromino> next = this.game.getPreview();

            int l = line - 2 * this.scale;

            sb.append(" ".repeat(this.scale * 2));
            this.appendStandaloneTetromino(sb, l % (3 * this.scale), next.get(l / (3 * this.scale)));
            sb.append(" ".repeat(this.scale * 2));
        }

        sb.append("\n");
    }

    /**
     * REQUIRES: this.game != null
     * <p>
     * EFFECTS: Create an array of strings as the graphical representation of
     * the current state of the game, using unicode box drawing characters. Each
     * element of the array corresponds to a line that can be printed to a
     * terminal to form a 2-dimensional drawing of the game.
     *
     * @return An array of String objects that represents the game
     */
    private String[] getGameRepresentation() {
        StringBuilder sb = new StringBuilder((this.scale * 18 + 5) * this.scale * 20);

        sb
                .append("  ".repeat(6 * this.scale))
                .append("\u250C").append("\u2500".repeat(this.scale * 20)).append("\u2510")
                .append("  ".repeat(6 * this.scale))
                .append("\n");

        for (int i = 0; i < this.scale * 20; i++) {
            this.appendLeftPadding(sb, i);

            int y = 19 - i / this.scale;
            for (int x = 0; x < 10; x++) {
                this.appendHorizontalBlock(sb, this.game.get(x, y) != 0);
            }

            this.appendRightPadding(sb, i);
        }

        sb
                .append("  ".repeat(6 * this.scale))
                .append("\u2514").append("\u2500".repeat(this.scale * 20)).append("\u2518")
                .append("  ".repeat(6 * this.scale))
                .append("\n");

        return sb.toString().split("\n");
    }

    /**
     * REQUIRES: this.screen != null and this.textGraphics != null and this.game
     * != null
     * <p>
     * MODIFIES: this.screen
     * <p>
     * EFFECTS: Draw the most up-to-date frame of the game to the terminal
     * screen.
     */
    @Override
    public void run() {
        this.updateTerminalSize();

        try {
            this.screen.clear();

            String[] text = this.getMenu() != null
                    ? this.getMenu().getRepresentation(this.terminalSize.getRows()) : this.getGameRepresentation();

            int x = this.getCenterLeftLimit(text[0].length());
            for (int i = 0; i < text.length; i++) {
                this.textGraphics.putString(x, i, text[i]);
            }

            this.checkInput();

            super.run();
            
            if (this.game != null) {
                this.textGraphics.putString(0, 0, String.format("FPS: %d", this.game.framerate()));
            }

            this.screen.refresh();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The "HOLD" text at scale 1-5. {@code CLI.HOLD_TEXT[scale - 1]} gives an
     * array of {@code length == scale}, with each element being a line to be
     * printed for the scaled text.
     */
    private static final String[][] HOLD_TEXT = new String[][]{
            {"  HOLD  "},
            {
                    " \u2599\u259F  \u259B\u259C  \u259B   \u259B\u259A ",
                    " \u2598\u259D  \u2580\u2580  \u2580\u2580  \u2580\u2580 ",
            },
            {
                    "   \u2588 \u2588  \u2588\u2580\u2588  \u2588    \u2588\u2580\u2584   ",
                    "   \u2588\u2584\u2588  \u2588 \u2588  \u2588    \u2588 \u2588   ",
                    "   \u2588 \u2588  \u2588\u2584\u2588  \u2588\u2584\u2584  \u2588\u2584\u2580   "
            },
            {
                    "  \u2588  \u2588     \u2588\u2588     \u2588       \u2588\u2588\u2588   ",
                    "  \u2588\u2584\u2584\u2588    \u2588  \u2588    \u2588       \u2588  \u2588  ",
                    "  \u2588\u2580\u2580\u2588    \u2588  \u2588    \u2588       \u2588  \u2588  ",
                    "  \u2588  \u2588     \u2588\u2588     \u2588\u2588\u2588\u2588    \u2588\u2588\u2588   "
            },
            {
                    "    \u2588   \u2588     \u2588\u2588\u2588     \u2588        \u2588\u2588\u2588\u2588     ",
                    "    \u2588   \u2588    \u2588   \u2588    \u2588        \u2588   \u2588    ",
                    "    \u2588\u2588\u2588\u2588\u2588    \u2588   \u2588    \u2588        \u2588   \u2588    ",
                    "    \u2588   \u2588    \u2588   \u2588    \u2588        \u2588   \u2588    ",
                    "    \u2588   \u2588     \u2588\u2588\u2588     \u2588\u2588\u2588\u2588\u2588    "
                            + "\u2588\u2588\u2588\u2588     "
            }
    };

    /**
     * The "NEXT" text at scale 1-5. {@code CLI.NEXT_TEXT[scale - 1]} gives an
     * array of {@code length == scale}, with each element being a line to be
     * printed for the scaled text.
     */
    private static final String[][] NEXT_TEXT = new String[][]{
            {"  NEXT  "},
            {
                    " \u259B\u259C  \u259B\u2598  \u259A\u259E  \u259C\u259B ",
                    " \u2598\u259D  \u2580\u2580  \u2598\u259D  \u259D\u2598 "
            },
            {
                    "   \u2588\u2580\u2588  \u2588\u2580\u2580  \u2588 \u2588  \u2588\u2588\u2588   ",
                    "   \u2588 \u2588  \u2588\u2584    \u2588    \u2588    ",
                    "   \u2588 \u2588  \u2588\u2584\u2584  \u2588 \u2588   \u2588    "
            },
            {
                    "  \u2588\u258C \u2588    \u2588\u2588\u2588\u2588    \u2588  \u2588    \u2588\u2588\u2588\u2588  ",
                    "  \u2588\u2590 \u2588    \u2588        \u259A\u259B      \u2590\u258C   ",
                    "  \u2588 \u258C\u2588    \u2588\u2580\u2580      \u259E\u2599      \u2590\u258C   ",
                    "  \u2588 \u2590\u2588    \u2588\u2584\u2584\u2584    \u2588  \u2588     \u2590\u258C   "
            },
            {
                    "    \u2588\u258C  \u2588    \u2588\u2588\u2588\u2588    \u2588   \u2588    "
                            + "\u2588\u2588\u2588\u2588\u2588    ",
                    "    \u2588\u2590\u258C \u2588    \u2588        \u2588 \u2588       \u2588      ",
                    "    \u2588 \u2588 \u2588    \u2588\u2588\u2588       \u2588        \u2588      ",
                    "    \u2588 \u2590\u258C\u2588    \u2588        \u2588 \u2588       \u2588      ",
                    "    \u2588  \u2590\u2588    \u2588\u2588\u2588\u2588    \u2588   \u2588      \u2588      "
            }
    };
}
