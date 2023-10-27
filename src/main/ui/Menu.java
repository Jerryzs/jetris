package ui;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private final List<Runnable> handlers;
    private final List<String> messages;

    private String title;

    private int active;

    public Menu(String title) {
        this.handlers = new ArrayList<Runnable>();
        this.messages = new ArrayList<String>();
        this.title = title;
    }

    public Menu add(String message) {
        return this.add(message, null);
    }

    public Menu add(String message, Runnable handler) {
        this.messages.add(message);
        this.handlers.add(handler);
        return this;
    }

    public Menu setHandler(Runnable r) {
        return this.setHandler(this.handlers.size() - 1, r);
    }

    public Menu setHandler(int i, Runnable r) {
        this.handlers.set(i, r);
        return this;
    }

    public void setMessage(String message) {
        this.setMessage(this.active, message);
    }

    public Menu setMessage(int i, String message) {
        this.messages.set(i, message);
        return this;
    }

    public void next() {
        if (this.active + 1 >= this.messages.size()) {
            return;
        }

        this.active++;
    }

    public void prev() {
        if (this.active - 1 < 0) {
            return;
        }

        this.active--;
    }

    public void trigger() {
        this.handlers.get(this.active).run();
    }

    public String[] getRepresentation(int height) {
        StringBuilder sb = new StringBuilder();

        int max = 0;
        for (int i = 0; i < this.messages.size(); i++) {
            sb.append(this.active == i ? "> " : "  ")
                    .append(this.messages.get(i))
                    .append("\n");
            max = Math.max(max, this.messages.get(i).length() + 4);
        }

        int padding = (max - this.title.length()) / 2;

        sb.insert(0, new StringBuilder()
                .append(" ".repeat(max))
                .append("\n".repeat((height - this.messages.size()) / 2 - 2))
                .append(" ".repeat(padding))
                .append(this.title)
                .append("\n\n"));

        return sb.toString().split("\n");
    }
}
