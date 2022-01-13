package frontend.panels;

import frontend.*;
import frontend.helpers.FieldPosition;
import frontend.helpers.Game;
import frontend.helpers.SocketListener;
import frontend.helpers.Stone;
import frontend.windows.DebugFrame;
import frontend.windows.GameFrame;
import interfaces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class GamePanel extends JPanel implements ActionListener {
    public static final int inventoryStonesStartY = 100;
    public static final int inventoryStonesOffsetX = 10;
    public static final int inventoryStonesOffsetY = 70;

    public static final int whiteStonesPosX = inventoryStonesOffsetX;
    public static final int blackStonesPosX = GameFrame.SCREEN_WIDTH - inventoryStonesOffsetX - 60;

    public static final int gridStart = 110;
    public static final int gridEnd = 710;
    public static final int middleOfGridStartAndEnd = (gridStart + gridEnd) / 2;

    public static final int distanceBetweenGridLines = 100;
    public static final int distanceBetweenThreeGridLines = gridStart + distanceBetweenGridLines * 2;
    public static final int oppositePositionBetweenThreeGridLines = gridEnd - distanceBetweenGridLines * 2;

    public static final int stoneOutOfBoundsPos = 1000;

    ArrayList<Stone> whiteStones = new ArrayList<>();
    ArrayList<Stone> blackStones = new ArrayList<>();
    ArrayList<Stone> allStones = new ArrayList<>();
    ArrayList<Stone> movableStones = new ArrayList<>();
    ArrayList<Stone> placedStones = new ArrayList<>();
    HashSet<FieldPosition> validPositions = new HashSet<>();

    Client client;
    Game game;

    String text = "";
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
            whiteStones.add(new Stone(GameInterface.COLOUR_WHITE, whiteStonesPosX, inventoryStonesStartY + i * inventoryStonesOffsetY));
        }

        for (int i = 0; i < 9; i++) {
            Stone stone = new Stone(GameInterface.COLOUR_BLACK, blackStonesPosX, inventoryStonesStartY + i * inventoryStonesOffsetY);
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
                    game.setStatus(gameStatus);

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

                            referencedStone.setGridPosition(xPos, yPos);

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

                            referencedStone.moveToTopLeftCorner(stoneOutOfBoundsPos, stoneOutOfBoundsPos);
                        }

                        case MoveStone -> {
                            Stone referencedStone = allStones.get(reference);

                            int toXPos = (int) arguments[2];
                            int toYPos = (int) arguments[3];

                            referencedStone.setGridPosition(toXPos, toYPos);

                            game.swapMoves();
                        }
                    }
                } catch (IOException ignored) {}

                currentlyClickedStone = null;
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

            // mouseClicked should handle this request, should there be a mill
            if (game.isThereAMill()) return;

            for (Stone stone : movableStones) {
                if (stone.contains(e.getPoint())) {
                    currentlyClickedStone = stone;
                    currentlyClickedStone.setDragStartPoint(new Point(
                            (int) currentlyClickedStone.getPoint().getX(),
                            (int) currentlyClickedStone.getPoint().getY()
                    ));
                    break;
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);

            // mouseClicked should handle this request, should there be a mill
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
                                    // MOVE_PHASE and JUMP_PHASE have identical parameters. Everything else is done in backend.logic.Game
                                    game.moveStone(
                                            allStones.indexOf(stone),
                                            currentlyClickedStone.getGridPosX(),
                                            currentlyClickedStone.getGridPosY(),
                                            validPosition.getGridX(),
                                            validPosition.getGridY()
                                    );
                                }
                            } catch (IllegalMoveException ex) {
                                ex.printStackTrace();

                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                            // Breaks out of the for loop early
                            return;
                        }
                    }

                    // There is a stone which the mouse is hovering over, but it
                    // has not been dropped over a dropzone
                    currentlyClickedStone.resetToDragStart();
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

        if (game.getPhase() != GamePhase.WAITING_FOR_PLAYERS && game.getPhase() != GamePhase.ABORTED) {
            g.drawString(
                    "You are: "
                    + game.getMyColourAsString(),
                    100,
                    40
            );
        }

        if (errorMessage.equals("It's the other player's turn.")
            && game.isItMyTurn()) {
            errorMessage = "Please use stones of your own colour to make a move.";
        }

        g.drawString(errorMessage, 350, 70);

        if (game.isThereAMill()) {
            text = (game.isThereAMill() ? "There is a mill" : "")
                    + " A stone of player "
                    + game.getCurrentPlayerAsString()
                    + " may be removed. Click the stone to do so.";

        } else if (game.getPhase() != GamePhase.WAITING_FOR_PLAYERS && game.getPhase() != GamePhase.ABORTED) {
            text = "It's "
                    + (game.isItMyTurn() ? "your" : (game.getCurrentPlayerAsString() + "'s"))
                    + " turn.";
        }

        if (game.getPhase() == GamePhase.GAME_OVER) {
            text = "GAME OVER LOL. "
                    + game.getOtherPlayerAsString()
                    + " won.";
        }

        g.drawString(text, 350, 50);
        g.drawString(game.getPhaseAsString(), 350, 25);

        for (int i = 0; i < 3; i++) {
            int j = i * distanceBetweenGridLines;
            g.drawLine(gridStart + j, gridStart + j, gridEnd - j, gridStart + j);
            g.drawLine(gridStart + j, gridEnd - j, gridEnd - j, gridEnd - j);

            g.drawLine(gridStart + j, gridStart + j, gridStart + j, gridEnd - j);
            g.drawLine(gridEnd - j, gridStart + j, gridEnd - j, gridEnd - j);
        }

        g.drawLine(middleOfGridStartAndEnd, gridStart, middleOfGridStartAndEnd, distanceBetweenThreeGridLines);
        g.drawLine(gridStart, middleOfGridStartAndEnd, distanceBetweenThreeGridLines, middleOfGridStartAndEnd);

        g.drawLine(middleOfGridStartAndEnd, gridEnd, middleOfGridStartAndEnd, oppositePositionBetweenThreeGridLines);
        g.drawLine(gridEnd, middleOfGridStartAndEnd, oppositePositionBetweenThreeGridLines, middleOfGridStartAndEnd);

        for (Stone stone: allStones) {
            stone.getIcon().paintIcon(this, g, (int) stone.getPoint().getX(), (int) stone.getPoint().getY());
        }
    }
}
