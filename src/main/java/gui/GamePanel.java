package gui;

import backend.Game;
import backend.Grid;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;

public class GamePanel extends JPanel {
    HashSet<Stone> whiteStones = new HashSet<>();
    HashSet<Stone> blackStones = new HashSet<>();
    HashSet<Stone> allStones = new HashSet<>();

    Game game;

    public GamePanel() {
        super();
        for (int i = 0; i < 9; i++) {
            whiteStones.add(new Stone(Grid.COLOUR_WHITE, 10, 100 + i*70));
        }

        for (int i = 0; i < 9; i++) {
            blackStones.add(new Stone(Grid.COLOUR_BLACK, 730, 100 + i*70));
        }

        allStones.addAll(blackStones);
        allStones.addAll(whiteStones);

        game = new Game();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        g.drawString("Weiß ist am Zug", 350, 50);

        System.out.println("Test");

        for (Stone stone: allStones) {
            stone.getIcon().paintIcon(this, g, (int) stone.getCurrentPoint().getX(), (int) stone.getCurrentPoint().getY());
        }

        for (int i = 0; i < 3; i++) {
            int j = i*100;
            g.drawLine(100 + j, 100 + j, 700 - j, 100 + j);
            g.drawLine(100 + j, 700 - j, 700 - j, 700 - j);

            g.drawLine(100 + j, 100 + j, 100 + j, 700 - j);
            g.drawLine(700 - j, 100 + j, 700 - j, 700 - j);
        }

        g.drawLine(400, 100, 400, 300);
        g.drawLine(100, 400, 300, 400);

        g.drawLine(400, 700, 400, 500);
        g.drawLine(700, 400, 500, 400);
    }
}
