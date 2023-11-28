package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class GUMenu extends JPanel implements Menu, KeyListener {
    private final List<JButton> buttons;

    private int active;

    protected GUMenu(String title, List<Item> items) {
        this.setLayout(new BorderLayout());

        this.setBackground(Color.WHITE);

        JLabel label = new JLabel(title);
        label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 128));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(new EmptyBorder(50, 0, 50, 0));

        this.add(label, BorderLayout.PAGE_START);

        this.buttons = new ArrayList<JButton>();
        this.add(this.buttons(items), BorderLayout.CENTER);
    }

    private JPanel buttons(List<Item> items) {
        JPanel buttonGroup = new JPanel();
        buttonGroup.setLayout(new BoxLayout(buttonGroup, BoxLayout.Y_AXIS));
        buttonGroup.setBackground(Color.WHITE);

        for (int i = 0; i < items.size(); i++) {
            JButton button = new JButton(items.get(i).getKey());
            button.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 32));
            button.addActionListener(items.get(i).getValue());
            button.setBorderPainted(false);
            button.setFocusPainted(false);
//            button.setContentAreaFilled(false);
            button.setBackground(this.active == i ? Color.black : Color.white);
            button.setForeground(this.active == i ? Color.white : Color.black);
            button.setBorder(new EmptyBorder(1, 2, 1, 2));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);

            buttons.add(button);
            buttonGroup.add(button, BorderLayout.CENTER);
        }

        return buttonGroup;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        int prev = this.active;

        if (key == KeyEvent.VK_W && this.active > 0) {
            this.active--;
        } else if (key == KeyEvent.VK_S && this.active < this.buttons.size() - 1) {
            this.active++;
        }

        if (prev != this.active) {
            this.buttons.get(prev).setForeground(Color.BLACK);
            this.buttons.get(prev).setBackground(Color.WHITE);
            this.buttons.get(this.active).setForeground(Color.WHITE);
            this.buttons.get(this.active).setBackground(Color.BLACK);
        }

        if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ENTER) {
            this.buttons.get(this.active).doClick();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void setMessage(String message) {
        this.buttons.get(this.active).setText(message);
    }

    protected static class Item extends AbstractMap.SimpleImmutableEntry<String, ActionListener> {
        public Item(String text, ActionListener action) {
            super(text, action);
        }
    }
}
