package frontend.panels;

import frontend.helpers.Game;
import interfaces.GameInterface;

import javax.swing.*;
import java.awt.*;

public class DebugPanel extends JPanel {
    Game game;

    private static final int firstColumn = 30;
    private static final int secondColumn = 120;

    public DebugPanel(Game game) {
        super();
        this.game = game;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawString(
                "In Inventory",
                firstColumn - 20,
                10
        );
        g.drawString(
                "White Stones",
                firstColumn,
                30
        );
        g.drawString(
                game.getStonesInInventory(GameInterface.COLOUR_WHITE).toString(),
                secondColumn,
                30
        );

        g.drawString(
                "Black Stones",
                firstColumn,
                50
        );

        g.drawString(
                game.getStonesInInventory(GameInterface.COLOUR_BLACK).toString(),
                secondColumn,
                50
        );


        g.drawString(
                "On the grid",
                firstColumn - 20,
                110
        );
        g.drawString(
                "White Stones",
                firstColumn,
                130
        );
        g.drawString(
                game.getStonesOnGrid(GameInterface.COLOUR_WHITE).toString(),
                secondColumn,
                130
        );

        g.drawString(
                "Black Stones",
                firstColumn,
                150
        );

        g.drawString(
                game.getStonesOnGrid(GameInterface.COLOUR_BLACK).toString(),
                secondColumn,
                150
        );

        g.drawString(
                "Is in jump phase",
                firstColumn - 20,
                210
        );
        g.drawString(
                "White Stones",
                firstColumn,
                230
        );
        g.drawString(
                game.isColourInJumpPhase(GameInterface.COLOUR_WHITE).toString(),
                secondColumn,
                230
        );

        g.drawString(
                "Black Stones",
                firstColumn,
                250
        );

        g.drawString(
                game.isColourInJumpPhase(GameInterface.COLOUR_BLACK).toString(),
                secondColumn,
                250
        );
    }
}
