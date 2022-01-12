package frontend.panels;

import frontend.*;
import frontend.helpers.FieldPosition;
import frontend.helpers.Game;
import frontend.helpers.SocketListener;
import frontend.helpers.Stone;
import frontend.windows.DebugFrame;
import interfaces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class GamePanel extends JPanel implements ActionListener {
    ArrayList<Stone> whiteStones = new ArrayList<>();
    ArrayList<Stone> blackStones = new ArrayList<>();
    ArrayList<Stone> allStones = new ArrayList<>();
    ArrayList<Stone> movableStones = new ArrayList<>();
    ArrayList<Stone> placedStones = new ArrayList<>();
    HashSet<FieldPosition> validPositions = new HashSet<>();

    Client client;
    Game game;

    String errorMessage = "";

    Stone currentlyClickedStone;
    private static final double dropZoneRadius = 30;
    DebugFrame debugFrame;

    public GamePanel(DebugFrame debugFrame, Game game) {
        super();

        this.debugFrame = debugFrame;
        this.game = game;
        this.client = game.getClient();

        for (int i = 0; i < 9; i++) {
            whiteStones.add(new Stone(GameInterface.COLOUR_WHITE, 10, 100 + i*70));
        }

        for (int i = 0; i < 9; i++) {
            Stone stone = new Stone(GameInterface.COLOUR_BLACK, 730, 100 + i*70);
            blackStones.add(stone);
        }

        for (int y = 0; y < this.game.getLimitX(); y++) {
            for (int x = 0; x < this.game.getLimitY(); x++) {
                if (game.isFieldPositionValid(x, y)) {
                    validPositions.add(new FieldPosition(x, y));
                }
            }
        }

        allStones.addAll(blackStones);
        allStones.addAll(whiteStones);
        movableStones.addAll(allStones);

        // GUI input event listener
        ClickListener clickListener = new ClickListener();
        DragListener dragListener = new DragListener();
        this.addMouseListener(clickListener);
        this.addMouseMotionListener(dragListener);

        // Client socket event listener
        SocketListener socketListener = new SocketListener(client.getObjectInputStream());

        socketListener.addPropertyChangeListener(e -> {
            if ("GameEvent".equals(e.getPropertyName())) {
                try {
                    GameEvent gameEvent = (GameEvent) e.getNewValue();
                    Object[] arguments = gameEvent.getArguments();
                    GameStatus gameStatus = gameEvent.getGameStatus();
                    int reference = gameEvent.getReference();

                    errorMessage = "";

                    switch (gameEvent.getMethod()) {
                        case Pong -> System.out.println("[GameEvent] Pong: " + Arrays.toString(gameEvent.getArguments()));
                        case Ping -> client.emit(new GameEvent(
                                GameEventMethod.Pong,
                                -1,
                                null,
                                "Client"
                        ));

                        case IllegalMove -> {
                            errorMessage = gameEvent.getArguments()[0].toString();

                            if (reference != -1) {
                                Stone referencedStone = allStones.get(reference);
                                try {
                                    referencedStone.resetToDragStart();
                                } catch (NullPointerException ignored) {}
                            }
                        }

                        case GameStart -> {
                            game.startGame((Boolean)arguments[0], (Boolean)arguments[1]);
                        }

                        case GameAborted -> {
                            errorMessage = gameEvent.getArguments()[0].toString();
                            game.abortGame();
                        }

                        case PlaceStone -> {
                            Stone referencedStone = allStones.get(reference);

                            int xPos = (int) arguments[0];
                            int yPos = (int) arguments[1];

                            referencedStone.setGridPositions(xPos, yPos);

                            movableStones.remove(referencedStone);
                            placedStones.add(referencedStone);

                            if (game.getPhase() == GamePhase.MOVE_PHASE) {
                                movableStones.addAll(placedStones);
                            }

                            game.swapMoves();
                        }

                        case RemoveStone -> {
                            Stone referencedStone = allStones.get(reference);

                            movableStones.remove(referencedStone);
                            placedStones.remove(referencedStone);
                            referencedStone.moveToTopLeftCorner(900,900);
                        }

                        case MoveStone -> {
                            Stone referencedStone = allStones.get(reference);

                            int xPos = (int) arguments[0];
                            int yPos = (int) arguments[1];

                            referencedStone.setGridPositions(xPos, yPos);

                            game.swapMoves();
                        }
                    }

                    repaint();
                    game.setStatus(gameStatus);

                } catch (IOException ignored) {}
                repaint();
            }
        });

        socketListener.execute();
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {

    }

    private class ClickListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            errorMessage = "";

            for (Stone stone : placedStones) {
                if (stone.contains(e.getPoint())) {
                    try {
                        game.removeStone(
                                allStones.indexOf(stone),
                                stone.getGridPosX(),
                                stone.getGridPosY()
                        );
                        break;
                    } catch (IllegalMoveException ignored) {

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);

            if (game.isThereAMill()) return;

            for (Stone stone : movableStones) {
                if (stone.contains(e.getPoint())) {
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

            for (Stone stone : movableStones) {
                if (stone.contains(e.getPoint())) {
                    for (FieldPosition validPosition : validPositions) {
                        double distance = Math.sqrt(
                                Math.pow(validPosition.getY() - e.getPoint().getY(), 2) +
                                Math.pow(validPosition.getX() - e.getPoint().getX(), 2)
                        );
                        if (distance <= dropZoneRadius) {
                            try {
                                if (game.getPhase() == GamePhase.PLACE_PHASE) {
                                    game.placeStone(
                                            allStones.indexOf(stone),
                                            validPosition.getGridX(),
                                            validPosition.getGridY(),
                                            stone.getColour()
                                    );
                                } else {
                                    // MOVE_PHASE and JUMP_PHASE have identical checks. Everything else is done in backend.logic.Game
                                    game.moveStone(
                                            allStones.indexOf(stone),
                                            currentlyClickedStone.getGridPosX(),
                                            currentlyClickedStone.getGridPosY(),
                                            validPosition.getGridX(),
                                            validPosition.getGridY()
                                    );
                                    currentlyClickedStone.setBeingDragged(false);
                                }

                                currentlyClickedStone = null;

                            } catch (IllegalMoveException ex) {
                                ex.printStackTrace();

                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            return;
                        }
                    }
                    // hier liegt der Fehler
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

        debugFrame.repaint();

        String text = "";

        if (game.getPhase() != GamePhase.WAITING_FOR_PLAYERS && game.getPhase() != GamePhase.ABORTED) {
            g.drawString("You are: " + game.getMyColourAsString(), 100, 40);
        }

        g.drawString(errorMessage, 350, 70);

        if (game.isThereAMill()) {
            text = (game.isThereAMill() ? "There is a mill" : "") + " A stone of player " + game.getCurrentPlayerAsString() + " may be removed. Click the stone to do so.";

        } else if (game.getPhase() != GamePhase.WAITING_FOR_PLAYERS && game.getPhase() != GamePhase.ABORTED) {
            text = "It's " + game.getCurrentPlayerAsString() + " turn.";
        }

        if (game.getPhase() == GamePhase.GAME_OVER) {
            text = "GAME OVER LOL. " + game.getOtherPlayer() + " won.";
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
