package gui;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    GamePanel gamePanel = new GamePanel();

    public GameFrame() {
        this.setTitle("Mühle für zwei Spieler");
        this.getContentPane().add(gamePanel, BorderLayout.CENTER);
        this.pack();
        this.setSize(812, 835);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
