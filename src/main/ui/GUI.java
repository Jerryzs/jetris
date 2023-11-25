package ui;

import model.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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

        this.mainMenu(null);
        this.startTimer();
    }

    @Override
    protected GMenu getMenu() {
        return (GMenu) super.getMenu();
    }

    @Override
    protected Menu getMainMenu() {
        return new GMenu("JETRIS", List.of(
                new GMenu.Item("Start", this::start),
                new GMenu.Item("Load save", this::load),
                new GMenu.Item("Exit", (e) -> System.exit(0))
        ));
    }

    @Override
    protected Menu getPauseMenu() {
        return new GMenu("PAUSED", List.of(
                new GMenu.Item("Resume", this::resume),
                new GMenu.Item("Save", this::save),
                new GMenu.Item("Main menu", this::mainMenu),
                new GMenu.Item("Exit", (e) -> System.exit(0))
        ));
    }

    private void save(ActionEvent e) {
        super.save();
    }

    private void load(ActionEvent e) {
        super.load();
        this.update();
    }

    private void start(ActionEvent e) {
        super.start();
        this.update();
    }

    private void pause(ActionEvent e) {
        super.pause();
        this.update();
    }

    private void resume(ActionEvent e) {
        super.resume();
        this.update();
    }

    private void mainMenu(ActionEvent e) {
        super.mainMenu();
        this.update();
    }

    private void update() {
        this.frame.getContentPane().removeAll();

        if (this.getMenu() != null) {
            this.frame.add(this.getMenu());
            this.frame.addKeyListener(this.getMenu());
        } else {
            GUI.GameGraphics graphics = new GUI.GameGraphics(this.game);
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

    private class GameGraphics extends JPanel implements KeyListener {
        private final Game game;

        private GameGraphics(Game game) {
            this.game = game;
            this.setBackground(Color.WHITE);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            int blockSize = (this.getHeight() / 20);
            int leftMargin = (this.getWidth() - 10 * blockSize) / 2;

            g2d.drawLine(leftMargin - blockSize, 0, leftMargin - blockSize, this.getHeight());
            g2d.drawLine(leftMargin + 11 * blockSize, 0, leftMargin + 11 * blockSize, this.getHeight());

            this.drawPlayfield(g2d, blockSize, leftMargin);
        }

        private void drawPlayfield(Graphics2D g, int s, int m) {
            for (int i = 19; i >= 0; i--) {
                for (int j = 0; j < 10; j++) {
                    int t = this.game.get(j, i);

                    if (t == 0) {
                        continue;
                    }

                    g.drawRect(m + j * s, (19 - i) * s, s, s);
                }
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_A:
                    this.game.moveLeft();
                    break;
                case KeyEvent.VK_D:
                    this.game.moveRight();
                    break;
                case KeyEvent.VK_S:
                    this.game.softDrop();
                    break;
                case KeyEvent.VK_W:
                    this.game.hardDrop();
                    break;
                case KeyEvent.VK_Q:
                    this.game.rotateLeft();
                    break;
                case KeyEvent.VK_E:
                    this.game.rotateRight();
                    break;
                case KeyEvent.VK_ESCAPE:
                    pause(null);
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }
}
