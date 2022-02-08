package backend.handlers;

import backend.Server;
import backend.helpers.Match;
import interfaces.User;
import backend.logic.Stone;
import interfaces.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerWorker extends Thread {
    private final Socket socket;
    private final Server server;
    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;

    private Match match;
    private boolean myColour;
    private User user;

    private boolean running = true;
    private final AtomicInteger unavailableCounter = new AtomicInteger(0);

    public ServerWorker(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        inputHandler();
    }

    public void setColour(boolean colour) {
        emit(new GameEvent(
                GameEventMethod.GameStart,
                -1,
                match.getGame().getStatus(),
                colour,
                match.getGame().getCurrentPlayer()
        ));
        myColour = colour;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public boolean isAuthenticated() {
        return user != null;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public boolean isInMatch() { return this.match != null; }

    public void emit(GameEvent event) {
        //DEBUG
        System.out.println("[ServerWorker] emit: " + event);

        try {
            objectOutputStream.writeObject(event);
            // this next statement cost me 3h to find / by @HerrMotz
            objectOutputStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("From Emit");
            disconnectHandler();
        }
    }

    private void inputHandler() {
        while (running) {
            try {
                if (objectInputStream.available() < 1 && unavailableCounter.incrementAndGet() > 1000) {
                    break;
                }
                eventHandler((GameEvent) objectInputStream.readObject());
            } catch (IOException | ClassNotFoundException ignored) {}
        }

        System.out.println("From InputHandler");
        disconnectHandler();
    }

    private void eventHandler(GameEvent event) {
        Object[] arguments = event.getArguments();

        int reference = event.getReference();

        // validate input reference number
        // only process positive numbers, because negatives are error codes
        if (reference > 0) {
            if ((event.getMethod() == GameEventMethod.RemoveStone)
                    == (myColour == GameInterface.COLOUR_WHITE)) {
                reference %= 9;
                reference += 9;
            } else {
                reference %= 9;
            }
        }

        try {
            switch (event.getMethod()) {
                case Ping -> emit(new GameEvent(
                        GameEventMethod.Pong,
                        -1,
                        null
                ));

                case Login -> {
                    String username = (String) arguments[0];
                    String password = (String) arguments[1];
                    emit(new GameEvent(
                            GameEventMethod.AuthResponse,
                            0,
                            null,
                            server.login(
                                    this,
                                    username,
                                    password
                            )
                    ));
                }
                case Register -> {
                    String username = (String) arguments[0];
                    String password = (String) arguments[1];
                    emit(new GameEvent(
                            GameEventMethod.AuthResponse,
                            0,
                            null,
                            server.register(
                                username,
                                password
                            )
                    ));
                }

                case Logout -> emit(new GameEvent(
                        GameEventMethod.AuthResponse,
                        0,
                        null,
                        server.logout(this)
                ));

                case EnterQuickMatchQueue ->
                        server.addToQuickMatchQueue(this);

                case LeaveQuickMatchQueue ->
                        server.removeFromQuickMatchQueue(this);

                case MatchRequest ->
                        server.relayMatchRequest(
                                (User) arguments[0],
                                getUser(),
                                this
                        );

                case MatchRequestResponse ->
                        server.relayMatchRequestResponse(
                                (User) arguments[0],
                                getUser(),
                                this,
                                (Boolean) arguments[1]
                        );

                case PlaceStone -> {
                    match.getGame().placeStoneCheckTurn(
                            myColour,
                            (Integer) arguments[0],
                            (Integer) arguments[1],
                            new Stone(myColour)
                    );

                    match.broadcast(new GameEvent(
                            GameEventMethod.PlaceStone,
                            reference,
                            match.getGame().getStatus(),
                            arguments[0],
                            arguments[1]
                    ));
                }
                case RemoveStone -> {
                    match.getGame().removeStoneCheckTurn(
                            myColour,
                            (Integer) arguments[0],
                            (Integer) arguments[1]
                    );

                    match.broadcast(new GameEvent(
                            GameEventMethod.RemoveStone,
                            reference,
                            match.getGame().getStatus(),
                            arguments[0],
                            arguments[1]
                    ));

                    if (match.getGame().getPhase() == GamePhase.GAME_OVER) {
                        match.broadcast(new GameEvent(
                                GameEventMethod.GameOver,
                                -1,
                                match.getGame().getStatus()
                        ));

                        match.endGame();
                    }
                }
                case MoveStone -> {
                    match.getGame().moveStoneCheckTurn(
                            myColour,
                            (Integer) arguments[0],
                            (Integer) arguments[1],
                            (Integer) arguments[2],
                            (Integer) arguments[3]
                    );

                    match.broadcast(new GameEvent(
                            GameEventMethod.MoveStone,
                            reference,
                            match.getGame().getStatus(),
                            arguments[0],
                            arguments[1],
                            arguments[2],
                            arguments[3]
                    ));
                }
            }
        } catch (IllegalMoveException e) {
            e.printStackTrace();

            if (event.getMethod() == GameEventMethod.RemoveStone) {
                // The stone should not be put back to the drag start point
                reference = -1;
            }

            emit(new GameEvent(
                    GameEventMethod.IllegalMove,
                    reference,
                    match.getGame().getStatus(),
                    e.getMessage()
            ));
        } catch (NullPointerException e) {
            e.printStackTrace();
            emit(new GameEvent(
                    GameEventMethod.IllegalMove,
                    reference,
                    null,
                    "The game has not started yet or there is no match linked to this ServerWorker"
            ));
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            emit(new GameEvent(
                    GameEventMethod.IllegalMove,
                    reference,
                    match.getGame().getStatus(),
                    "Your client gave parameters of wrong type. Please update your programme!"
            ));
        }
    }

    public void returnToLobby() {
        setMatch(null);
        server.broadcastPlayerPool();
    }

    private void disconnectHandler() {
        System.out.println("DisconnectHandler");
        try {
            socket.close();
        } catch (IOException ignored) {}

        server.removeServerWorker(this);

        if (match != null) {
            match.abortGame(this);
        }

        running = false;
    }
}
