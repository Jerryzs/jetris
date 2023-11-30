package ui;

import model.Game;
import persistence.Save;

import java.util.Timer;
import java.util.TimerTask;

public abstract class UserInterface extends TimerTask {
    private final int framerate;

    protected Game game;

    private int frameCounter;
    private long frameCountStartTime;

    private Save save;
    private Menu menu;

    protected UserInterface(int framerate) {
        this.framerate = framerate;
    }

    protected void startTimer() {
        Timer timer = new Timer();
        timer.schedule(this, 0, 500 / this.framerate);
    }

    protected Menu getMenu() {
        return this.menu;
    }

    protected void setMenu(Menu menu) {
        this.menu = menu;
    }

    protected void save() {
        this.menu.setMessage(this.save.store() ? "Saved!" : "Failed to save");
    }

    protected void load() {
        this.frameCountStartTime = System.currentTimeMillis();
        this.frameCounter = 0;

        this.save = new Save();
        this.game = this.save.load(this.framerate);

        if (this.game == null) {
            this.menu.setMessage("Load failed");
            return;
        }

        this.menu = null;

        this.frameCountStartTime = System.currentTimeMillis();
    }

    protected void start() {
        this.frameCountStartTime = System.currentTimeMillis();
        this.frameCounter = 0;

        this.game = new Game(this.framerate);
        this.save = new Save(this.game);
        this.menu = null;

        this.frameCountStartTime = System.currentTimeMillis();
    }

    protected void pause() {
        this.game.toggleGame();
        this.menu = this.getPauseMenu();
    }

    protected void resume() {
        this.game.toggleGame();
        this.menu = null;
    }

    protected void mainMenu() {
        this.game = null;
        this.menu = this.getMainMenu();
    }

    protected void exit() {
        System.exit(0);
    }

    protected abstract Menu getMainMenu();

    protected abstract Menu getPauseMenu();

    @Override
    public void run() {
        if (this.game != null) {
            this.game.run();
            this.countFrame();
        }
    }

    private void countFrame() {
        this.frameCounter++;

        if (System.currentTimeMillis() - frameCountStartTime >= 1000) {
            this.game.framerate(this.frameCounter);
            this.frameCounter = 0;
            this.frameCountStartTime = System.currentTimeMillis();
        }
    }
}
