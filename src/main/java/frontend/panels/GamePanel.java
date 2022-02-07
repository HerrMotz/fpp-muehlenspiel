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
import java.util.*;
import java.util.List;

/**
 * Game Panel
 *
 * This is the main window of the game.
 * Here the player can see the game's board, make moves, see error messages when
 * an invalid has been made and whose turn it is.
 */
public class GamePanel extends JPanel implements ActionListener {
    // GUI Drawing statics
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

    public static final int removedStoneCrossSize = 20;
    public static final int removedStoneCrossDelta = 3;
    public static final int removedStoneCrossOffset = 30;
    public static final int removedStoneCrossDelta1 = removedStoneCrossSize - removedStoneCrossDelta;
    public static final int removedStoneCrossDelta2 = removedStoneCrossSize + removedStoneCrossDelta;

    // Collections to store all game elements
    // allStones is drawn at the very bottom of method repaint()
    private ArrayList<Stone> allStones;
    private ArrayList<Stone> movableStones;
    private ArrayList<Stone> placedStones;
    private HashSet<FieldPosition> validPositions;

    private final Client client;
    private final Game game;

    private String text = "";
    private String errorMessage = "";

    private Stone currentlyClickedStone;
    private Point indicatorOfLastMove;
    private Point indicatorOfMovedStone;
    private Point indicatorOfRemovedStone;

    private static final double dropZoneRadius = 30;
    private final DebugFrame debugFrame;

    private User user;
    private ClientMode clientMode = ClientMode.Login;
    private boolean uiChanged = false;

    private Set<Object> loggedInUsers = new HashSet<>();

    // UI Elements
    private final GridLayout loginLayout = new GridLayout(0,1,10,10);
    private final GridLayout registerLayout = new GridLayout(0,1,10,10);
    private final GridLayout quickMatchLayout = new GridLayout(0,1,10,10);
    private final GridLayout lobbyLayout = new GridLayout(0,1,10,10);

    private final JButton btnLogin = new JButton("Login");
    private final JButton btnLogout = new JButton("Logout");
    private final JButton btnRegister = new JButton("Register");
    private final JButton btnRegisterMenu = new JButton("Register");

    private final JButton btnQuickMatch = new JButton("Quickmatch!");
    private final JButton btnBack = new JButton("← Back");

    private final JButton btnInvite = new JButton("Invite Player");

    private final JTextField txtUsername = new JTextField();
    private final JTextField txtPassword = new JTextField();

    private final JLabel lblUsername = new JLabel("Username");
    private final JLabel lblPassword = new JLabel("Password");

    private final JLabel lblMillgame = new JLabel("Nine men's morris");
    private final JLabel lblUsernameShow = new JLabel("");

    private final JLabel lblErrorMessage = new JLabel();

    @SuppressWarnings("rawtypes")
    private final JList lsUser = new JList();
    JScrollPane listScroller = new JScrollPane(lsUser);

    /**
     * Starts a fresh game board for a new game.
     */
    public void initNewGame() {
        setClientMode(ClientMode.Game);

        // The order in which white and black stones are added to "all stones" matters
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

        // The order in which white and black stones are added to "all stones" matters
        allStones.addAll(whiteStones);
        allStones.addAll(blackStones);

        if (game.getMyColour() == GameInterface.COLOUR_WHITE) {
            movableStones.addAll(whiteStones);
        } else {
            movableStones.addAll(blackStones);
        }
    }

    /**
     * Constructor
     *
     * @param debugFrame the window for the DebugPanel. Required to trigger a repaint of the debug panel when there is a game event.
     * @param game Helper class to manage the communication with the server.
     */
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

                    System.out.println("[GameEvent Handler]: " + gameEvent);

                    // extract all properties
                    Object[] arguments = gameEvent.getArguments();
                    GameStatus gameStatus = gameEvent.getGameStatus();
                    int reference = gameEvent.getReference();
                    game.setStatus(gameStatus);

                    indicatorOfMovedStone = null;
                    indicatorOfLastMove = null;
                    indicatorOfRemovedStone = null;
                    resetErrorMessage();

                    switch (gameEvent.getMethod()) {
                        case Pong -> System.out.println("[GameEvent] Pong: " + Arrays.toString(gameEvent.getArguments()));
                        case Ping -> client.emit(new GameEvent(
                                GameEventMethod.Pong,
                                -1,
                                null,
                                "Client"
                        ));

                        case AuthResponse -> {
                            AuthenticationResponse authResponse = (AuthenticationResponse) arguments[0];
                            switch (authResponse.getMethod()) {
                                case Login -> {
                                    if (authResponse.isSuccess()) {
                                        setUser(authResponse.getUser());

                                        setErrorMessage(authResponse.getMessage());
                                        setClientMode(ClientMode.Lobby);
                                        btnLogout.setEnabled(true);

                                        rerender();
                                    } else {
                                        setErrorMessage(authResponse.getMessage());
                                        rerender();
                                    }
                                }

                                case Logout -> {
                                    if (authResponse.isSuccess()) {
                                        setUser(null);
                                        btnLogout.setEnabled(false);
                                    }
                                }

                                case Register -> {
                                    if (authResponse.isSuccess()) {
                                        setClientMode(ClientMode.Login);
                                    }
                                }
                            }
                            return;
                        }

                        case BroadcastPlayerPool -> {
                            loggedInUsers = new HashSet<>();
                            loggedInUsers.addAll(List.of(arguments));
                            rerender();
                        }

                        case MatchRequest -> {
                            User user = (User) arguments[0];
                            Object[] options = {
                                    "Yes (starts game)",
                                    "No (politely declines)"
                            };

                            int selection = JOptionPane.showOptionDialog(
                                    this,
                                    "You got a match request from user " + user.getUsername(),
                                    "Match Request",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    options,
                                    options[1]
                            );

                            client.emit(new GameEvent(
                                    GameEventMethod.MatchRequestResponse,
                                    0,
                                    null,
                                    user,
                                    selection < 1
                            ));
                        }

                        case MatchRequestResponse -> {
                            User user = (User) arguments[0];
                            boolean accepted = (Boolean) arguments[1];

                            new Thread(() -> JOptionPane.showMessageDialog(
                                    null,
                                    "Player " + user.getUsername() + " has "
                                            + (accepted ? "accepted" : "declined")
                                            + " your game request",
                                    "Response from " + user.getUsername(),
                                    JOptionPane.INFORMATION_MESSAGE
                            )).start();

                        }

                        case IllegalMove -> {
                            setErrorMessage(gameEvent.getArguments()[0].toString());

                            if (reference > 0) {
                                Stone referencedStone = allStones.get(reference);
                                try {
                                    referencedStone.resetToDragStart();
                                } catch (NullPointerException ignored) {}
                            }
                        }

                        case GameStart -> {
                            game.startGame((Boolean)arguments[0], (Boolean)arguments[1]);
                            initNewGame();
                        }

                        case GameAborted -> {
                            setErrorMessage(gameEvent.getArguments()[0].toString());
                            game.abortGame();
                        }

                        case GameOver -> movableStones = new ArrayList<>();

                        case PlaceStone -> {
                            Stone referencedStone = allStones.get(reference);

                            int xPos = (int) arguments[0];
                            int yPos = (int) arguments[1];

                            referencedStone.setGridPosition(xPos, yPos);

                            indicatorOfMovedStone = referencedStone.getPoint();

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

                            indicatorOfRemovedStone = new Point(referencedStone.getPoint());

                            movableStones.remove(referencedStone);
                            placedStones.remove(referencedStone);

                            referencedStone.moveToTopLeftCorner(stoneOutOfBoundsPos, stoneOutOfBoundsPos);

                            game.swapMoves();
                        }

                        case MoveStone -> {
                            Stone referencedStone = allStones.get(reference);
                            indicatorOfMovedStone = referencedStone.getPoint();

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

                currentlyClickedStone = null;
                rerender();
            }
        });

        socketListener.execute();

        // Button events
        btnLogin.addActionListener(e -> {

            // check whether fields are empty
            if (txtUsername.getText().isBlank() || txtPassword.getText().isBlank()) {
                setErrorMessage("You have to enter credentials to login.");
            } else {
                try {
                    client.emit(new GameEvent(
                            GameEventMethod.Login,
                            0,
                            null,
                            txtUsername.getText(),
                            txtPassword.getText()
                    ));
                } catch (IOException ex) {
                    setErrorMessage("Failed to send credentials to server: " + ex);
                }
            }

            rerender();
        });

        btnLogout.addActionListener(e -> {
            try {
                client.emit(new GameEvent(GameEventMethod.Logout, -1, null));
            } catch (IOException ex) {
                setErrorMessage("Failed to logout: " + ex);
            }

            setClientMode(ClientMode.Login);

            rerender();
        });

        btnRegisterMenu.addActionListener(e -> {
            setClientMode(ClientMode.Register);
            rerender();
        });

        btnRegister.addActionListener(e -> {
            setClientMode(ClientMode.Login);
            if (txtUsername.getText().isBlank() || txtPassword.getText().isBlank()) {
                setErrorMessage("You have to enter credentials to login.");
            } else {
                try {
                    client.emit(new GameEvent(
                            GameEventMethod.Register,
                            0,
                            null,
                            txtUsername.getText(),
                            txtPassword.getText()
                    ));
                } catch (IOException ex) {
                    setErrorMessage("An error occurred while registering: " + ex);
                }
            }
            rerender();
        });

        btnQuickMatch.addActionListener(e -> {
            try {
                client.emit(new GameEvent(
                        GameEventMethod.EnterQuickMatchQueue,
                        0,
                        null,
                        (Object) null
                ));
                setClientMode(ClientMode.QuickMatch);
                rerender();
            } catch (IOException ex) {
                errorMessage = "An error occurred while entering quick match queue: " + ex;
            }
        });

        btnBack.addActionListener(e -> {
            if (getClientMode() == ClientMode.QuickMatch) {
                // client emit withdrawal from quick match queue
                try {
                    client.emit(new GameEvent(
                            GameEventMethod.LeaveQuickMatchQueue,
                            0,
                            null,
                            (Object) null
                    ));
                } catch (IOException ex) {
                    errorMessage = "An error occurred while entering quick match queue: " + ex;
                }
            }
            setClientMode(ClientMode.Login);
            rerender();
        });

        btnInvite.addActionListener(e -> {
            try {
                client.emit(new GameEvent(
                        GameEventMethod.MatchRequest,
                        0,
                        null,
                        lsUser.getSelectedValue()
                ));
            } catch (IOException ex) {
                errorMessage = "An error occurred while sending a match request: " + ex;
            }
        });

        // Settings for UI elements
        lblUsername.setHorizontalAlignment(JLabel.CENTER);
        lblPassword.setHorizontalAlignment(JLabel.CENTER);

        lblMillgame.setHorizontalAlignment(JLabel.CENTER);
        lblMillgame.setFont(new Font("Serif", Font.PLAIN, 20));

        lblErrorMessage.setForeground(Color.RED);
        lblErrorMessage.setHorizontalAlignment(JLabel.CENTER);

        btnLogout.setEnabled(false);

        lsUser.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        lsUser.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        lsUser.setVisibleRowCount(-1);

        listScroller.setPreferredSize(new Dimension(250, 80));
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {

    }

    private boolean isLoggedIn() {
        return user != null;
    }

    private User getUser() {
        return user;
    }

    private void setUser(User user) {
        this.user = user;
    }

    private ClientMode getClientMode() {
        return clientMode;
    }

    private void setClientMode(ClientMode clientMode) {
        this.clientMode = clientMode;
    }

    private boolean isUiChanged() {
        boolean temp = uiChanged;
        uiChanged = false;
        return temp;
    }

    private void triggerUiChange() {
        uiChanged = true;
    }

    private class ClickListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            errorMessage = "";
            System.out.println(getClientMode());

            if (placedStones != null) {
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
        }

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);

            if (game.getPhase() == GamePhase.ABORTED || game.getPhase() == GamePhase.GAME_OVER) {
                setClientMode(ClientMode.Lobby);
            }

            // mouseClicked should handle this request, should there be a mill
            if (game.isThereAMill()) return;

            if (movableStones == null) return;

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

            if (movableStones != null) {
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
                                                validPosition.getGridY()
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
                                    setErrorMessage(ex.getMessage());

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
                rerender();
            }
        }
    }

    private class DragListener extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (currentlyClickedStone != null) {
                currentlyClickedStone.moveToCenter((int)e.getPoint().getX(), (int)e.getPoint().getY());
                rerender();
            }
        }
    }

    private void drawGame(Graphics g) {
        if (getLayout() != null) {
            removeAll();
            setLayout(null);
            rerender();
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
            setErrorMessage("Please use stones of your own colour to make a move.");
        }

        g.setColor(Color.RED);
        g.drawString(errorMessage, 350, 70);
        g.setColor(Color.BLACK);

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
            phase = "Game Over" ;
            text = "GAME OVER LOL. "
                    + game.getOtherPlayerAsString()
                    + " won.";
        } else if (game.isColourInJumpPhase(game.getMyColour())) {
            phase = "Jump Phase";
        } else if (game.getPhase() == GamePhase.ABORTED) {
            phase = "Game aborted. Press any button to go back to lobby...";
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
        if (allStones != null) {
            for (Stone stone : allStones) {
                stone.getIcon().paintIcon(this, g, (int) stone.getPoint().getX(), (int) stone.getPoint().getY());
            }
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

        if (indicatorOfRemovedStone != null) {
            g.setColor(Color.RED);

            final int indicatorX = removedStoneCrossOffset + indicatorOfRemovedStone.x;
            final int indicatorY = removedStoneCrossOffset + indicatorOfRemovedStone.y;

            int[] x = {
                    indicatorX - removedStoneCrossDelta1,
                    indicatorX - removedStoneCrossDelta2,
                    indicatorX + removedStoneCrossDelta1,
                    indicatorX + removedStoneCrossDelta2
            };

            int[] y = {
                    indicatorY - removedStoneCrossDelta2,
                    indicatorY - removedStoneCrossDelta1,
                    indicatorY + removedStoneCrossDelta2,
                    indicatorY + removedStoneCrossDelta1
            };

            int[] x2 = {
                    indicatorX + removedStoneCrossDelta1,
                    indicatorX + removedStoneCrossDelta2,
                    indicatorX - removedStoneCrossDelta1,
                    indicatorX - removedStoneCrossDelta2
            };

            int[] y2 = {
                    indicatorY - removedStoneCrossDelta2,
                    indicatorY - removedStoneCrossDelta1,
                    indicatorY + removedStoneCrossDelta2,
                    indicatorY + removedStoneCrossDelta1
            };

            g.fillPolygon(x, y, 4);

            g.fillPolygon(x2, y2, 4);

            g.setColor(Color.BLACK);
        }
    }

    private void drawLobby(Graphics g) {
        setLayout(lobbyLayout);

        lblErrorMessage.setText(errorMessage);

        if (loggedInUsers != null) {
            //noinspection unchecked
            lsUser.setListData(loggedInUsers.toArray());
        }

        System.out.println(getUser());
        if (getUser() != null) {
            lblUsernameShow.setText("Your username: " + getUser().getUsername() + " Your user id: " + getUser().getId());
        }

        add(lblMillgame);
        add(lblErrorMessage);
        add(lblUsernameShow);

        add(lsUser);
        add(btnInvite);

        add(btnLogout);
    }

    private void drawLogin(Graphics g) {
        setLayout(loginLayout);

        lblErrorMessage.setText(errorMessage);

        add(lblMillgame);
        add(lblUsernameShow);
        add(lblErrorMessage);
        add(lblUsername);
        add(txtUsername);
        add(lblPassword);
        add(txtPassword);
        add(btnLogin);
        add(btnRegisterMenu);
        add(btnLogout);
        add(btnQuickMatch);
    }

    private void drawRegister(Graphics g) {
        setLayout(registerLayout);

        add(lblMillgame);

        add(lblUsername);
        add(txtUsername);

        add(lblPassword);
        add(txtPassword);

        add(btnRegister);
        add(btnBack);
    }

    private void drawQuickMatch(Graphics g) {
        setLayout(quickMatchLayout);

        add(lblMillgame);
        add(new JLabel("Waiting for another player..."));

        add(btnBack);
    }

    private void rerender() {
        triggerUiChange();
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        g.setColor(Color.black);

        if (debugFrame != null) {
            debugFrame.repaint();
        }

        boolean changes = isUiChanged();

        if (changes) {
            removeAll();
            txtUsername.setText("");
            txtPassword.setText("");
        }

        switch (getClientMode()) {
            case Game -> drawGame(g);
            case Lobby -> drawLobby(g);
            case Login -> drawLogin(g);
            case Register -> drawRegister(g);
            case QuickMatch -> drawQuickMatch(g);
        }

        if (changes) {
            validate();
            repaint();
        }
    }

    private void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    private void resetErrorMessage() {
        errorMessage = "";
    }
}
