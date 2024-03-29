package frontend.windows;

import frontend.helpers.Game;
import frontend.panels.GamePanel;

import javax.swing.*;
import java.awt.*;

/**
 * Frame for the Game panel
 */
public class GameFrame extends JFrame {
    GamePanel gamePanel;

    public static final int SCREEN_WIDTH = 820;
    public static final int SCREEN_HEIGHT = 820;

    public GameFrame(DebugFrame debugFrame, Game game) {
        gamePanel = new GamePanel(debugFrame, game);

        this.setTitle("Mühle für n Spieler");
        this.getContentPane().add(gamePanel, BorderLayout.CENTER);
        this.pack();
        this.setSize(SCREEN_WIDTH+12, SCREEN_HEIGHT+35);
        this.setResizable(false);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
