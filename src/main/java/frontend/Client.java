package frontend;

import frontend.helpers.Game;
import frontend.windows.DebugFrame;
import frontend.windows.GameFrame;
import interfaces.GameEvent;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

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

    public void emit(GameEvent event) throws IOException {
        System.out.println("[Emit] " + event.getMethod() + ": " + Arrays.toString(event.getArguments()));
        objectOutputStream.flush();
        objectOutputStream.writeObject(event);
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 2302);
        Game game = new Game(client);

        if (client.connect()) {
            Runnable gui = () -> {
                DebugFrame debugFrame = new DebugFrame(game, true);
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
