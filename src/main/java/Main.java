import gui.GameFrame;

import javax.swing.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        Runnable gui = () -> {
            GameFrame gameFrame = new GameFrame();
        };
        SwingUtilities.invokeLater(gui);
    }
}
