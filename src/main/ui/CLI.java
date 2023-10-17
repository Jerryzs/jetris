package ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import model.Game;
import model.tetromino.AbstractTetromino;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@SuppressWarnings({"AvoidEscapedUnicodeCharacters", "SuppressWarnings"})
public class CLI implements Runnable {
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

    private int scale;

    private final Game game;

    private final TerminalScreen screen;
    private final TextGraphics textGraphics;
    private TerminalSize terminalSize;

    public CLI(InputStream in, OutputStream out, int refreshRate) {
        try {
            this.screen = new DefaultTerminalFactory(out, in, StandardCharsets.UTF_8).createScreen();
            this.screen.startScreen();
            this.screen.setCursorPosition(null);
            this.textGraphics = this.screen.newTextGraphics();
            this.scale = Math.max(this.screen.getTerminalSize().getRows() / 22, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.game = new Game(this, refreshRate);

//        Timer timer = new Timer();
//        timer.schedule(this, 0, 1000 / CLI.REFRESH_RATE);
    }

    private void checkInput() throws IOException {
        KeyStroke key = this.screen.pollInput();

        if (key != null) {
            if (key.getKeyType() == KeyType.EOF) {
                System.exit(0);
            }

            if (key.getKeyType() == KeyType.Escape) {
                this.game.toggleGame();
            }

            if (key.getKeyType() == KeyType.Character) {
                if (!this.game.isPaused()) {
                    this.handleInput(Character.toLowerCase(key.getCharacter()));
                }

                if (key.getCharacter() == '=' && this.scale < 5) {
                    this.scale++;
                } else if (key.getCharacter() == '-' && this.scale > 1) {
                    this.scale--;
                }
            }
        }
    }

    private void handleInput(char key) {
        if (key == 'w') {
            this.game.hold();
        } else if (key == 'a') {
            this.game.moveLeft();
        } else if (key == 's') {
            this.game.softDrop();
        } else if (key == 'd') {
            this.game.moveRight();
        } else if (key == 'q') {
            this.game.rotateLeft();
        } else if (key == 'e') {
            this.game.rotateRight();
        } else if (key == ' ') {
            this.game.hardDrop();
        }
    }

    private void updateTerminalSize() {
        TerminalSize newSize = screen.doResizeIfNecessary();
        if (newSize != null) {
            this.terminalSize = newSize;
        }
    }

    private int getCenterLeftLimit(int width) {
        if (this.terminalSize == null || this.terminalSize.getColumns() < width) {
            return 0;
        }

        return this.terminalSize.getColumns() / 2 - width / 2;
    }

    private void appendHorizontalBlock(StringBuilder sb, boolean condition) {
        sb.append((condition ? "\u2588\u2588" : "  ").repeat(this.scale));
    }

    private void appendStandaloneTetromino(StringBuilder sb, int line, AbstractTetromino tetromino) {
        int l = line / this.scale;
        for (int k = 0; k < 4; k++) {
            this.appendHorizontalBlock(sb, tetromino.getStandalone()[4 * l + k] != 0);
        }
    }

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

    private void appendRightPadding(StringBuilder sb, int line) {
        sb.append("\u2502");

        if (line < this.scale) {
            sb.append(" ".repeat(this.scale * 2))
                    .append(CLI.NEXT_TEXT[this.scale - 1][line])
                    .append(" ".repeat(this.scale * 2));
        } else if (line < 2 * this.scale
                || (line - 2 * this.scale) % (3 * this.scale) >= 2 * this.scale
                || line > 13 * this.scale) {
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

    @Override
    public void run() {
        this.updateTerminalSize();

        try {
            this.screen.clear();

            String[] text = this.getGameRepresentation();

            int x = this.getCenterLeftLimit(text[0].length());
            for (int i = 0; i < text.length; i++) {
                textGraphics.putString(x, i, text[i]);
            }

            this.screen.refresh();
            this.checkInput();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
