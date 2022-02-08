package frontend.windows;

import frontend.helpers.Game;
import frontend.panels.DebugPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Frame for the Debug panel
 */
public class DebugFrame extends JFrame {
    DebugPanel debugPanel;

    public static final int SCREEN_WIDTH = 320;
    public static final int SCREEN_HEIGHT = 640;

    public DebugFrame(Game game, boolean visible) {
        debugPanel = new DebugPanel(game);
        this.setTitle("Debug Info");
        this.getContentPane().add(debugPanel, BorderLayout.CENTER);
        this.pack();
        this.setSize(SCREEN_WIDTH+12, SCREEN_HEIGHT+35);
        this.setResizable(false);
        this.setVisible(visible);
        this.setLocation(1400,200);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }
}
