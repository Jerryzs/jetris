package ui;

import model.Game;
import model.tetromino.AbstractTetromino;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

public class GUI extends UserInterface {
    private final JFrame frame;

    public GUI(int framerate) {
        super(framerate);

        this.frame = new JFrame();
        this.frame.setBackground(Color.WHITE);

        this.frame.setSize(1280, 720);
        this.frame.setVisible(true);
        this.frame.setFocusable(true);
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.mainMenu();
        this.startTimer();
    }

    @Override
    protected GUMenu getMenu() {
        return (GUMenu) super.getMenu();
    }

    @Override
    protected Menu getMainMenu() {
        return new GUMenu("JETRIS", List.of(
                new GUMenu.Item("Start", e -> this.start()),
                new GUMenu.Item("Load save", e -> this.load()),
                new GUMenu.Item("Exit", e -> System.exit(0))
        ));
    }

    @Override
    protected Menu getPauseMenu() {
        return new GUMenu("PAUSED", List.of(
                new GUMenu.Item("Resume", e -> this.resume()),
                new GUMenu.Item("Save", e -> this.save()),
                new GUMenu.Item("Main menu", e -> this.mainMenu()),
                new GUMenu.Item("Exit", e -> System.exit(0))
        ));
    }

    @Override
    protected void save() {
        super.save();
    }

    @Override
    protected void load() {
        super.load();
        this.update();
    }

    @Override
    protected void start() {
        super.start();
        this.update();
    }

    @Override
    protected void pause() {
        super.pause();
        this.update();
    }

    @Override
    protected void resume() {
        super.resume();
        this.update();
    }

    @Override
    protected void mainMenu() {
        super.mainMenu();
        this.update();
    }

    private void update() {
        this.frame.getContentPane().removeAll();

        for (KeyListener listener : this.frame.getKeyListeners()) {
            this.frame.removeKeyListener(listener);
        }

        if (this.getMenu() != null) {
            this.frame.add(this.getMenu());
            this.frame.addKeyListener(this.getMenu());
        } else {
            GUI.GameGraphics graphics = new GUI.GameGraphics(this.game, this);
            this.frame.add(graphics);
            this.frame.addKeyListener(graphics);
        }

        this.frame.revalidate();
    }

    @Override
    public void run() {
        super.run();
        this.frame.repaint();
    }

    private static class GameGraphics extends JPanel implements KeyListener {
        private final Game game;

        private final GUI gui;

        private GameGraphics(Game game, GUI gui) {
            this.game = game;
            this.gui = gui;
            this.setBackground(Color.WHITE);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            int blockSize = (this.getHeight() / 20);

            int top = 0;
            int bottom = blockSize * 20;

            int playfieldLeft = (this.getWidth() - 10 * blockSize) / 2;
            int playfieldRight = playfieldLeft + 10 * blockSize;

            int holdRight = playfieldLeft - blockSize / 2;
            int holdLeft = holdRight - blockSize * 5;
            int holdBottom = blockSize * 5;

            int nextLeft = playfieldRight + blockSize / 2;
            int nextRight = nextLeft + blockSize * 5;
            int nextBottom = blockSize * 17;

            this.drawPlayfield(g2d, blockSize, playfieldLeft, playfieldRight, top, bottom);
            this.drawHold(g2d, blockSize, holdLeft, holdRight, top, holdBottom);
            this.drawNext(g2d, blockSize, nextLeft, nextRight, top, nextBottom);
        }

        private void drawPlayfield(Graphics2D g, int s, int pl, int pr, int pt, int pb) {
            g.drawLine(pl, pt, pl, pb);
            g.drawLine(pr, pt, pr, pb);
            g.drawLine(pl, pb, pr, pb);

            for (int i = 19; i >= 0; i--) {
                for (int j = 0; j < 10; j++) {
                    int t = this.game.get(j, i);

                    if (t == 0) {
                        continue;
                    }

                    g.drawRect(pl + j * s, (19 - i) * s, s, s);
                }
            }
        }

        private void drawHold(Graphics2D g, int s, int hl, int hr, int ht, int hb) {
            AbstractTetromino t = this.game.getHold();

            this.drawOpenBoxWithHeading("HOLD", g, s, hl, hr, ht, hb);

            if (t == null) {
                return;
            }

            this.drawStandaloneTetromino(t, g, s, hl, s);
        }

        private void drawNext(Graphics2D g, int s, int nl, int nr, int nt, int nb) {
            List<AbstractTetromino> next = this.game.getPreview();

            this.drawOpenBoxWithHeading("NEXT", g, s, nl, nr, nt, nb);

            for (int i = 0; i < next.size(); i++) {
                this.drawStandaloneTetromino(next.get(i), g, s, nl, s + i * 3 * s);
            }
        }

        private void drawOpenBoxWithHeading(String h, Graphics2D g, int s, int l, int r, int t, int b) {
            g.drawLine(l, t, l, b);
            g.drawLine(r, t, r, b);
            g.drawLine(l, b, r, b);

            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, this.getHeight() / 25));
            g.drawString(h, l + s * 1.5f, s);
        }

        private void drawStandaloneTetromino(AbstractTetromino at, Graphics2D g, int s, int l, int t) {
            int[] standalone = at.getStandalone();

            for (int i = 0; i < standalone.length; i++) {
                if (standalone[i] == 0) {
                    continue;
                }

                g.drawRect(l + s / 2 + i % 4 * s, t + s + i / 4 * s, s, s);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int c = e.getKeyCode();

            if (c == KeyEvent.VK_A) {
                this.game.moveLeft();
            } else if (c == KeyEvent.VK_D) {
                this.game.moveRight();
            } else if (c == KeyEvent.VK_S) {
                this.game.softDrop();
            } else if (c == KeyEvent.VK_W) {
                this.game.hardDrop();
            } else if (c == KeyEvent.VK_Q) {
                this.game.rotateLeft();
            } else if (c == KeyEvent.VK_E) {
                this.game.rotateRight();
            } else if (c == KeyEvent.VK_ESCAPE) {
                this.gui.pause();
            } else if (c == KeyEvent.VK_SPACE) {
                this.game.hold();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
    }
}
