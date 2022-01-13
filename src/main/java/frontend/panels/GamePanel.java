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

    public static final int panelBorderThickness = 10;
    public static final int screenWidthMinusPanelBorderThickness = GameFrame.SCREEN_WIDTH-panelBorderThickness;
    public static final int screenHeightMinusPanelBorderThickness = GameFrame.SCREEN_HEIGHT-panelBorderThickness;

    public static final int indicatorCircleDiameter = 30;
    public static final int indicatorCircleAroundStoneDiameter = 80;

    private ArrayList<Stone> allStones = new ArrayList<>();
    private ArrayList<Stone> movableStones = new ArrayList<>();
    private ArrayList<Stone> placedStones = new ArrayList<>();
    private HashSet<FieldPosition> validPositions = new HashSet<>();

    private final Client client;
    private final Game game;

    private String text = "";
    private String errorMessage = "";

    private Stone currentlyClickedStone;
    private Point indicatorOfLastMove;
    private Point indicatorOfMovedStone;

    private static final double dropZoneRadius = 30;
    private final DebugFrame debugFrame;

    public void initNewGame() {
        ArrayList<Stone> whiteStones = new ArrayList<>();
        ArrayList<Stone> blackStones = new ArrayList<>();
        allStones = new ArrayList<>();
        movableStones = new ArrayList<>();
        placedStones = new ArrayList<>();
        validPositions = new HashSet<>();

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
    }

    public GamePanel(DebugFrame debugFrame, Game game) {
        super();

        this.debugFrame = debugFrame;
        this.game = game;
        this.client = game.getClient();

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
                            initNewGame();
                            game.startGame((Boolean)arguments[0], (Boolean)arguments[1]);
                        }

                        case GameAborted -> {
                            errorMessage = gameEvent.getArguments()[0].toString();
                            game.abortGame();
                        }

                        case GameOver -> movableStones = new ArrayList<>();

                        case PlaceStone -> {
                            Stone referencedStone = allStones.get(reference);

                            int xPos = (int) arguments[0];
                            int yPos = (int) arguments[1];

                            referencedStone.setGridPosition(xPos, yPos);

                            indicatorOfMovedStone = referencedStone.getPoint();
                            System.out.println("indicator " + indicatorOfMovedStone);

                            movableStones.remove(referencedStone);
                            placedStones.add(referencedStone);

                            if (game.getPhase() == GamePhase.MOVE_PHASE) {
                                movableStones.addAll(placedStones);
                            }

                            if (!game.isThereAMill()) {
                                game.swapMoves();
                            }
                        }

                        case RemoveStone -> {
                            Stone referencedStone = allStones.get(reference);
                            indicatorOfMovedStone = referencedStone.getPoint();
                            System.out.println("indicator" + indicatorOfMovedStone);

                            movableStones.remove(referencedStone);
                            placedStones.remove(referencedStone);

                            referencedStone.moveToTopLeftCorner(stoneOutOfBoundsPos, stoneOutOfBoundsPos);

                            game.swapMoves();
                        }

                        case MoveStone -> {
                            Stone referencedStone = allStones.get(reference);
                            indicatorOfMovedStone = referencedStone.getPoint();
                            System.out.println("indicator" + indicatorOfMovedStone);

                            int posX = (int) arguments[0];
                            int posY = (int) arguments[1];
                            int toPosX = (int) arguments[2];
                            int toPosY = (int) arguments[3];

                            referencedStone.setGridPosition(toPosX, toPosY);
                            indicatorOfLastMove = new FieldPosition(posX, posY);

                            if (!game.isThereAMill()) {
                                game.swapMoves();
                            }
                        }
                    }
                } catch (IOException ignored) {}

                System.out.println("currentlyClickedStone null");
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
                    System.out.println("[CurrentlyClickedStone] New currently clicked stone" + currentlyClickedStone.hashCode());

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
                                            allStones.indexOf(currentlyClickedStone), // ICH HASSE MEIN LEBEN DAFÜR HABE ICH 4 H gebraucht
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

                    // es liegt
                    currentlyClickedStone.resetToDragStart();
                    currentlyClickedStone = null;
                }
            }
            repaint();
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

        if (debugFrame != null) {
            debugFrame.repaint();
        }

        if (game.getPhase() != GamePhase.WAITING_FOR_PLAYERS && game.getPhase() != GamePhase.ABORTED) {
            g.drawString(
                    "You are: "
                    + game.getMyColourAsString(),
                    100,
                    40
            );
        }

        if (game.isItMyTurn()) {
            g.setColor(Color.GREEN);
            g.fillRect(0, 0, GameFrame.SCREEN_WIDTH, panelBorderThickness);
            g.fillRect(0, 0, panelBorderThickness, GameFrame.SCREEN_HEIGHT);
            g.fillRect(screenWidthMinusPanelBorderThickness, panelBorderThickness, panelBorderThickness, GameFrame.SCREEN_HEIGHT);
            g.fillRect(0, screenHeightMinusPanelBorderThickness, GameFrame.SCREEN_WIDTH, panelBorderThickness);
            g.setColor(Color.BLACK);
        }

        if (errorMessage.equals("It's the other player's turn.")
            && game.isItMyTurn()) {
            errorMessage = "Please use stones of your own colour to make a move.";
        }

        g.drawString(errorMessage, 350, 70);

        if (game.isThereAMill()) {
            text = "There is a mill";
            if (game.getCurrentPlayerAsString().equals(game.getMyColourAsString())) {
                text += " A stone of player "
                        + game.getOtherPlayerAsString()
                        + " may be removed. Click the stone to do so.";
            } else {
                text = "Your opponent may remove one of your stones.";
            }

        } else if (game.getPhase() != GamePhase.WAITING_FOR_PLAYERS && game.getPhase() != GamePhase.ABORTED) {
            text = "It's "
                    + (game.isItMyTurn() ? "your" : (game.getCurrentPlayerAsString() + "'s"))
                    + " turn.";
        }

        g.drawString(text, 350, 50);

        String phase;

        if (game.getPhase() == GamePhase.GAME_OVER) {
            phase = "Game Over";
            text = "GAME OVER LOL. "
                    + game.getOtherPlayerAsString()
                    + " won.";
        } else if (game.isColourInJumpPhase(game.getMyColour())) {
            phase = "JUMP_PHASE";
        } else {
            phase = game.getPhaseAsString();
        }

        g.drawString(phase, 350, 25);

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


        // This draws all stones on the grid by iterating the allStones set
        for (Stone stone: allStones) {
            stone.getIcon().paintIcon(this, g, (int) stone.getPoint().getX(), (int) stone.getPoint().getY());
        }

        // This draws the indicator for the last move
        if (indicatorOfLastMove != null) {
            g.setColor(Color.ORANGE);
            g.fillOval(
                indicatorOfLastMove.x - indicatorCircleDiameter / 2,
                indicatorOfLastMove.y - indicatorCircleDiameter / 2,
                indicatorCircleDiameter,
                indicatorCircleDiameter
            );
            g.setColor(Color.BLACK);
        }

        if (indicatorOfMovedStone != null) {
            g.setColor(Color.ORANGE);
            g.drawOval(
                    indicatorOfMovedStone.x-10,
                    indicatorOfMovedStone.y-10,
                    indicatorCircleAroundStoneDiameter,
                    indicatorCircleAroundStoneDiameter
            );
            g.drawOval(
                    indicatorOfMovedStone.x-20,
                    indicatorOfMovedStone.y-20,
                    indicatorCircleAroundStoneDiameter + 20,
                    indicatorCircleAroundStoneDiameter + 20
            );
            g.setColor(Color.BLACK);
        }
    }
}
