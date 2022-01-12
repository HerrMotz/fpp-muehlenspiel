package backend;

import backend.logic.Stone;
import interfaces.GameEvent;
import interfaces.GameEventMethod;
import interfaces.IllegalMoveException;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerWorker extends Thread {
    private final Socket socket;
    private final Server server;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private boolean running = true;

    private boolean myColour;

    private final AtomicInteger unavailableCounter = new AtomicInteger(0);

    public ServerWorker(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());

            inputHandler();
        } catch (IOException e) {
            e.printStackTrace();
            disconnectHandler();
        }
    }

    public void setMyColour(boolean colour) {
        myColour = colour;
    }

    public void emit(GameEvent event) {
        try {
            objectOutputStream.writeObject(event);
        } catch (IOException e) {
            e.printStackTrace();
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

        disconnectHandler();
    }

    private void eventHandler(GameEvent event) {
        Object[] arguments = event.getArguments();
        int reference = event.getReference();

        try {
            switch (event.getMethod()) {
                case Ping -> emit(new GameEvent(
                        GameEventMethod.Pong,
                        -1,
                        null
                ));
                case PlaceStone -> {
                    server.getGame().placeStone(
                            myColour,
                            (Integer) arguments[0],
                            (Integer) arguments[1],
                            new Stone((Boolean) arguments[2])
                    );

                    server.broadcast(new GameEvent(
                            GameEventMethod.PlaceStone,
                            reference,
                            server.getGame().getStatus(),
                            arguments[0],
                            arguments[1]
                    ));
                }
                case RemoveStone -> {
                    server.getGame().removeStone(
                            myColour,
                            (Integer) arguments[0],
                            (Integer) arguments[1]
                    );

                    server.broadcast(new GameEvent(
                            GameEventMethod.RemoveStone,
                            reference,
                            server.getGame().getStatus(),
                            arguments[0],
                            arguments[1]
                    ));
                }
                case MoveStone -> {
                    server.getGame().moveStone(
                            myColour,
                            (Integer) arguments[0],
                            (Integer) arguments[1],
                            (Integer) arguments[2],
                            (Integer) arguments[3]
                    );

                    server.broadcast(new GameEvent(
                            GameEventMethod.MoveStone,
                            reference,
                            server.getGame().getStatus(),
                            arguments[0],
                            arguments[1],
                            arguments[2],
                            arguments[3]
                    ));
                }
            }

            System.out.println("[GameEvent] " + this.getName() + " " + (myColour ? "White" : "Black"));
            System.out.println(server.getGame());
            System.out.println(event.getMethod());
            System.out.println(Arrays.toString(event.getArguments()));
            System.out.println("It's " + (server.getGame().getCurrentPlayer() ? "White" : "Black") + "'s turn");

        } catch (IllegalMoveException e) {
            e.printStackTrace();
            emit(new GameEvent(
                    GameEventMethod.IllegalMove,
                    reference,
                    server.getGame().getStatus(),
                    e.getMessage()
            ));
        } catch (NullPointerException e) {
            e.printStackTrace();
            emit(new GameEvent(
                    GameEventMethod.IllegalMove,
                    reference,
                    server.getGame().getStatus(),
                    "The game has not started yet."
            ));
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            emit(new GameEvent(
                    GameEventMethod.IllegalMove,
                    reference,
                    server.getGame().getStatus(),
                    "Your client gave parameters of wrong type. Please update your programme!"
            ));
        }
    }

    private void disconnectHandler() {
        try {
            socket.close();
        } catch (IOException ignored) {}
        server.removeServerWorker(this);

        running = false;
    }
}
