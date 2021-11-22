package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameFrame extends JFrame {
    GamePanel gamePanel = new GamePanel();

    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 800;

    public GameFrame() {
        this.setTitle("Mühle für zwei Spieler");
        this.getContentPane().add(gamePanel, BorderLayout.CENTER);
        this.pack();
        this.setSize(SCREEN_WIDTH+12, SCREEN_HEIGHT+35);
        this.setResizable(false);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
