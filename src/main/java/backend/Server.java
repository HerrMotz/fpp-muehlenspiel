package backend;

import backend.handlers.ServerWorker;
import backend.logic.Game;
import interfaces.GameEvent;
import interfaces.GameEventMethod;
import interfaces.IllegalMoveException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Server extends Thread {
    private final int port;
    private final ConcurrentHashMap<ServerWorker, Boolean> serverWorkers = new ConcurrentHashMap<>();
    private final AtomicInteger serverWorkersSize = new AtomicInteger(0);
    private Game game;

    public Server(int port) throws IllegalMoveException {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on " + port);

            //noinspection InfiniteLoopStatement
            while (true) {
                if (serverWorkersSize.get() < 2) {
                    Socket clientSocket = serverSocket.accept();

                    ServerWorker serverWorker = new ServerWorker(this, clientSocket);
                    serverWorkers.put(serverWorker, false);
                    serverWorkersSize.incrementAndGet();

                    serverWorker.start();

                    System.out.println("Connected to: " + clientSocket);
                    System.out.println("Client count: " + serverWorkersSize.get());

                    if (serverWorkersSize.get() == 2) {
                        // DEBUG
                        System.out.println("Start game");
                        game = new Game(true, .5);

                        // Tell each player his colour, and which colour starts
                        AtomicBoolean temp = new AtomicBoolean(false);
                        serverWorkers.replaceAll((k, v) -> temp.getAndSet(!temp.get()));

                        for (Map.Entry<ServerWorker, Boolean> entry : serverWorkers.entrySet()) {
                            entry.getKey().emit(new GameEvent(
                                    GameEventMethod.GameStart,
                                    -1,
                                    game.getStatus(),
                                    entry.getValue(),
                                    game.getCurrentPlayer()
                            ));

                            // Give the ServerWorker / Client Handler the player's colour for validation.
                            entry.getKey().setMyColour(entry.getValue());
                        }
                    }
                }
            }
        } catch (IOException ioException) {
            System.err.println("An error occurred while communicating with a client: " + ioException);
        } catch (IllegalMoveException e) {
            System.err.println("Could not start game! There is an error in the code.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public synchronized void broadcast(GameEvent event) {
        for (ServerWorker serverWorker : serverWorkers.keySet()) {
            serverWorker.emit(event);
        }
    }

    public void removeServerWorker(ServerWorker serverWorker) {
        serverWorkers.remove(serverWorker);
        serverWorkersSize.decrementAndGet();

        // DEBUG
        System.out.println("Client count: " + serverWorkersSize.get());

        if (serverWorkersSize.get() > 0) {
            broadcast(new GameEvent(
                    GameEventMethod.GameAborted,
                    -1,
                    game.getStatus(),
                    "Other player disconnected"
            ));
        }
    }

    public Game getGame() {
        return game;
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(2302);
            server.start();
        } catch (IllegalMoveException e) {
            System.err.println("Could not instantiate a game: " + e);
        }
    }
}
