import backend.Game;
import backend.IllegalMoveException;
import gui.DebugFrame;
import gui.GameFrame;

import javax.swing.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        try {
            Game game = new Game(true, 0.5);

            Runnable gui = () -> {
                DebugFrame debugFrame = new DebugFrame(game, true);
                GameFrame gameFrame = new GameFrame(debugFrame, game);
            };

            SwingUtilities.invokeLater(gui);

        } catch (IllegalMoveException e) {
            System.exit(-1);
        }
    }
}
