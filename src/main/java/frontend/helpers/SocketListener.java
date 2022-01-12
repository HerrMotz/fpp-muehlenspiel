package frontend.helpers;

import interfaces.GameEvent;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketListener extends SwingWorker<Void, Void> {
    private final ObjectInputStream objectInputStream;
    private final AtomicInteger unavailableCounter = new AtomicInteger(0);

    public SocketListener(ObjectInputStream objectInputStream) {
        this.objectInputStream = objectInputStream;
    }

    @Override
    protected Void doInBackground() {
        System.out.println("[Client SocketListener] start");
        while (true) {
            try {
                if (objectInputStream.available() < 1 && unavailableCounter.incrementAndGet() > 1000) {
                    System.out.println("[Client SocketListener] Socket closed");
                    break;
                }
                Object event = objectInputStream.readObject();
                firePropertyChange("GameEvent", null, event);
                System.out.println("[Client SocketListener] event " + ((GameEvent) event).getMethod() + " " + Arrays.toString(((GameEvent) event).getArguments()));
            } catch (IOException | ClassNotFoundException ignored) {}
        }
        System.out.println("[Client SocketListener] end");
        return null;
    }
}
