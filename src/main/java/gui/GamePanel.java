package gui;

import backend.Game;
import backend.Grid;
import backend.IllegalMoveException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;

public class GamePanel extends JPanel implements ActionListener {
    HashSet<Stone> whiteStones = new HashSet<>();
    HashSet<Stone> blackStones = new HashSet<>();
    HashSet<Stone> allStones = new HashSet<>();
    HashSet<Stone> moveableStones = new HashSet<>();
    HashSet<Stone> placedStones = new HashSet<>();

    HashSet<FieldPosition> validPositions = new HashSet<>();

    Game game;

    Stone currentlyClickedStone;

    double dropZoneRadius = 30;

    public GamePanel() {
        super();

        try {
            game = new Game();
        } catch (IllegalMoveException ignored) {
            return;
        }

        for (int i = 0; i < 9; i++) {
            whiteStones.add(new Stone(Grid.COLOUR_WHITE, 10, 100 + i*70));
        }

        for (int i = 0; i < 9; i++) {
            Stone stone = new Stone(Grid.COLOUR_BLACK, 730, 100 + i*70);
            blackStones.add(stone);
        }

        for (int y = 0; y < game.getLimitX(); y++) {
            for (int x = 0; x < game.getLimitY(); x++) {
                try {
                    game.checkValidityOfFieldPosition(x, y);
                    validPositions.add(new FieldPosition(x, y));
                } catch (IllegalMoveException ignored) {}
            }
        }

        allStones.addAll(blackStones);
        allStones.addAll(whiteStones);

        moveableStones.addAll(allStones);

        ClickListener clickListener = new ClickListener();
        DragListener dragListener = new DragListener();

        this.addMouseListener(clickListener);
        this.addMouseMotionListener(dragListener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    private class ClickListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (game.isThereAMill()) {
                for (Stone stone : placedStones) {
                    if (stone.contains(e.getPoint())) {
                        try {
                            game.removeStone(stone.getGridPosX(), stone.getGridPosY());
                            moveableStones.remove(stone);
                            placedStones.remove(stone);
                            stone.moveToTopLeftCorner(900,900);
                            repaint();
                            break;
                        } catch (IllegalMoveException ignored) {}
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);

            if (game.isThereAMill()) return;

            for (Stone stone : moveableStones) {
                if (stone.contains(e.getPoint())) {
                    stone.setPreviousPoint(e.getPoint());
                    currentlyClickedStone = stone;

                    if (!currentlyClickedStone.isBeingDragged()) {
                        currentlyClickedStone.setDragStartPoint(
                                new Point(
                                        (int) currentlyClickedStone.getCurrentPoint().getX(),
                                        (int) currentlyClickedStone.getCurrentPoint().getY()
                                )
                        );
                        currentlyClickedStone.setBeingDragged(true);
                    }
                    break;
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);

            if (game.isThereAMill()) return;

            for (Stone stone : moveableStones) {
                if (stone.contains(e.getPoint())) {
                    for (FieldPosition validPosition : validPositions) {
                        double distance = Math.sqrt(
                                Math.pow(validPosition.getY() - e.getPoint().getY(), 2) +
                                Math.pow(validPosition.getX() - e.getPoint().getX(), 2)
                        );
                        if (distance <= dropZoneRadius) {
                            try {
                                if (game.getPhase() == Game.PLACE_PHASE) {
                                    game.placeStone(validPosition.getGridX(), validPosition.getGridY(), new backend.Stone(stone.getColour()));
                                    currentlyClickedStone.moveToCenter((int) validPosition.getX(), (int) validPosition.getY());
                                    repaint();

                                    currentlyClickedStone.setGridPosX(validPosition.getGridX());
                                    currentlyClickedStone.setGridPosY(validPosition.getGridY());
                                    moveableStones.remove(currentlyClickedStone);
                                    placedStones.add(currentlyClickedStone);
                                    currentlyClickedStone.setBeingDragged(false);

                                    if (game.getPhase() == Game.MOVE_PHASE) {
                                        moveableStones.addAll(placedStones);
                                    }
                                } else {
                                    // MOVE_PHASE and JUMP_PHASE have identical checks. Everything else is done in backend.Game
                                    game.moveStone(currentlyClickedStone.getGridPosX(), currentlyClickedStone.getGridPosY(), validPosition.getGridX(), validPosition.getGridY());
                                    currentlyClickedStone.moveToCenter((int) validPosition.getX(), (int) validPosition.getY());
                                    repaint();

                                    currentlyClickedStone.setGridPosX(validPosition.getGridX());
                                    currentlyClickedStone.setGridPosY(validPosition.getGridY());
                                    currentlyClickedStone.setBeingDragged(false);
                                }

                                currentlyClickedStone = null;
                            } catch (IllegalMoveException ex) {
                                currentlyClickedStone.moveToTopLeftCorner((int) stone.getDragStartPoint().getX(), (int) stone.getDragStartPoint().getY());
                                stone.setBeingDragged(false);
                                repaint();
                            }
                            return;
                        }
                    }
                    currentlyClickedStone.moveToTopLeftCorner((int) stone.getDragStartPoint().getX(), (int) stone.getDragStartPoint().getY());
                    stone.setBeingDragged(false);
                    repaint();
                }
            }
        }
    }

    private class DragListener extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (currentlyClickedStone != null) {
                currentlyClickedStone.moveToCenter((int)e.getPoint().getX(), (int)e.getPoint().getY());
            }
            repaint();
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        String text;
        if (game.isThereAMill()) {
            text = (game.isThereAMill() ? "Es gibt eine Mühle" : "") + " Ein Stein von " + game.getCurrentPlayer() + " darf entfernt werden.";
        } else {
            text = game.getCurrentPlayer() + " ist am Zug.";
        }
        if (game.getPhase() == Game.GAME_OVER) {
            text = "GAME OVER LOL. " + game.getCurrentPlayer() + " hat gewonnen.";
        }
        g.drawString(text, 350, 50);
        g.drawString(game.getPhaseAsString(), 350, 25);

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

        for (Stone stone: allStones) {
            stone.getIcon().paintIcon(this, g, (int) stone.getCurrentPoint().getX(), (int) stone.getCurrentPoint().getY());
        }
    }
}
