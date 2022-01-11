package backend;

import backend.logic.Game;
import interfaces.GameEvent;
import interfaces.GameEventMethod;
import interfaces.IllegalMoveException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class Server extends Thread {
    private final int port;
    private final HashSet<ServerWorker> serverWorkers = new HashSet<>();
    private Game game;
    private final HashMap<ServerWorker, Boolean> playerColours = new HashMap<>();

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
                if (serverWorkers.size() < 2) {
                    Socket clientSocket = serverSocket.accept();
                    //noinspection BusyWait
                    Thread.sleep(1000);
                    System.out.println("Test");

                    ServerWorker serverWorker = new ServerWorker(this, clientSocket);
                    serverWorkers.add(serverWorker);
                    serverWorker.start();

                    System.out.println("Connected to: " + clientSocket);
                    System.out.println("Client count: " + serverWorkers.size());

                    if (serverWorkers.size() == 2) {
                        System.out.println("Start game");
                        game = new Game(true, .5);

                        Iterator<ServerWorker> iterator = serverWorkers.iterator();
                        playerColours.put(iterator.next(), false);
                        playerColours.put(iterator.next(), true);

                        // Tell each player his colour, and which colour starts
                        for (Map.Entry<ServerWorker, Boolean> entry : playerColours.entrySet()) {
                            entry.getKey().emit(new GameEvent(
                                    GameEventMethod.GameStart,
                                    entry.getValue(),
                                    game.getCurrentPlayer()
                            ));
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(GameEvent event) {
        for (ServerWorker serverWorker : serverWorkers) {
            serverWorker.emit(event);
        }
    }

    public HashSet<ServerWorker> getServerWorkers() {
        return serverWorkers;
    }

    public void removeServerWorker(ServerWorker serverWorker) {
        serverWorkers.remove(serverWorker);
        System.out.println("Client count: " + serverWorkers.size());
        if (serverWorkers.size() < 2) {
            broadcast(new GameEvent(GameEventMethod.GameAborted, "Other player disconnected"));
        } else {
            broadcast(new GameEvent(GameEventMethod.ClientDisconnect, serverWorker.getName()));
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