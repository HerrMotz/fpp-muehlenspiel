package gui;

import backend.Game;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    GamePanel gamePanel;

    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 800;

    public GameFrame(DebugFrame debugFrame, Game game) {
        gamePanel = new GamePanel(debugFrame, game);
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
