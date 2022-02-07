package frontend;

import frontend.helpers.Game;
import frontend.windows.DebugFrame;
import frontend.windows.GameFrame;
import interfaces.GameEvent;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * Nine men's morris - Game Client
 *
 * starts GUI & Socket connection
 *
 * @author Max Stock, Daniel Motz
 * @version 3.0 Deluxe
 */
public class Client {
    private final String serverHost;
    private final int serverPort;
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public Client(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    /**
     * Connect the client's socket to a given server
     * @return whether the connection was successful
     */
    public boolean connect() {
        try {
            socket = new Socket(serverHost, serverPort);
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send a game event via the client's socket
     * Uses Java's object output stream
     *
     * @param event GameEvent, e.g. a move by the player. Contains a GameMethod (which kind of move was made) and parameters (to which field, from which field etc.)
     * @throws IOException Should there be an issue with the socket connection (e.g. closed / unavailable)
     */
    public void emit(GameEvent event) throws IOException {
        System.out.println("[Emit] " + event.getMethod() + ": " + Arrays.toString(event.getArguments()));
        objectOutputStream.flush();
        objectOutputStream.writeObject(event);
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }

    /**
     * Starts a client
     * spins up a game window, a debug window and a SocketListener for game events emitted by the server.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Client client = new Client("localhost", 2302);
        Game game = new Game(client);

        if (client.connect()) {
            Runnable gui = () -> {
                DebugFrame debugFrame = null;
                if (args.length > 0) {
                  debugFrame = new DebugFrame(game, true);
                }
                GameFrame gameFrame = new GameFrame(debugFrame, game);
            };

            SwingUtilities.invokeLater(gui);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    client.socket.close();
                } catch (IOException ignored) {}
            }));
        } else {
            System.err.println("Could not connect to server");
        }
    }
}
