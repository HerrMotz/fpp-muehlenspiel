package backend;

import backend.logic.Game;
import interfaces.GameEvent;
import interfaces.GameEventMethod;
import interfaces.IllegalMoveException;
import interfaces.StoneInterface;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerWorker extends Thread {
    private final Socket socket;
    private final Server server;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private final AtomicInteger unavailableCounter = new AtomicInteger(0);

    public ServerWorker(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        System.out.println("ServerWorker spun up for client: " + socket + " on server: " + server);
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

    public void emit(GameEvent event) {
        try {
            objectOutputStream.writeObject(event);
        } catch (IOException e) {
            disconnectHandler();
        }
    }

    private void inputHandler() {
        while (true) {
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
        System.out.println("[Server] Event handler");
        Object[] arguments = event.getArguments();
        try {
            switch (event.getMethod()) {
                case Ping -> emit(new GameEvent(GameEventMethod.Pong));
                case PlaceStone -> {
                    server.getGame().placeStone(
                            (Integer) arguments[1],
                            (Integer) arguments[2],
                            (StoneInterface) arguments[3]
                    );

                    server.broadcast(new GameEvent(
                            GameEventMethod.PlaceStone, arguments[0], arguments[1], arguments[2]
                    ));
                }
                case RemoveStone -> {
                    server.getGame().removeStone(
                            (Integer) arguments[1],
                            (Integer) arguments[2]
                    );

                    server.broadcast(new GameEvent(
                            GameEventMethod.RemoveStone, arguments[0], arguments[1], arguments[2]
                    ));
                }
                case MoveStone -> {
                    server.getGame().moveStone(
                            (Integer) arguments[1],
                            (Integer) arguments[2],
                            (Integer) arguments[3],
                            (Integer) arguments[4]
                    );

                    server.broadcast(new GameEvent(
                            GameEventMethod.MoveStone, arguments[0], arguments[1], arguments[2], arguments[3], arguments[4]
                    ));
                }
            }

            System.out.println(server.getGame());
            System.out.println(event.getMethod());
            System.out.println(Arrays.toString(event.getArguments()));

        } catch (IllegalMoveException e) {
            e.printStackTrace();
            emit(new GameEvent(GameEventMethod.IllegalMove, e.getMessage()));
        } catch (NullPointerException e) {
            e.printStackTrace();
            emit(new GameEvent(GameEventMethod.IllegalMove, "The game has not started yet."));
        } catch (ClassCastException e) {
            e.printStackTrace();
            emit(new GameEvent(GameEventMethod.IllegalMove, "Your client gave parameters of wrong type. Please update your programme!"));
        }
    }

    private void disconnectHandler() {
        try {
            socket.close();
        } catch (IOException ignored) {}
        server.removeServerWorker(this);
    }
}
