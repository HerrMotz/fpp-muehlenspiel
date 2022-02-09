package backend.helpers;

import backend.handlers.ServerWorker;
import backend.logic.Game;
import interfaces.GameEvent;
import interfaces.GameEventMethod;
import interfaces.IllegalMoveException;

public class Match {
    public static final double x = .5;

    private final ServerWorker serverWorker1;
    private final ServerWorker serverWorker2;
    private final Game game;

    public Match(ServerWorker serverWorker1, ServerWorker serverWorker2) throws IllegalMoveException {
        this.serverWorker1 = serverWorker1;
        this.serverWorker2 = serverWorker2;

        this.serverWorker1.setMatch(this);
        this.serverWorker2.setMatch(this);

        // instantiate game and determine start player
        this.game = new Game(true, x);

        // determine which player gets which colour
        boolean colour1 = (Math.random() < x);
        this.serverWorker1.setColour(colour1);
        this.serverWorker2.setColour(!colour1);

    }

    public Game getGame() {
        return game;
    }

    public void endGame() {
        serverWorker1.addToPool();
        serverWorker2.addToPool();
        serverWorker1.returnToLobby();
        serverWorker2.returnToLobby();
    }

    public void broadcast(GameEvent gameEvent) {
        serverWorker1.emit(gameEvent);
        serverWorker2.emit(gameEvent);
    }

    public void abortGame(ServerWorker disconnectedClient) {
        GameEvent gameAbortedEvent = new GameEvent(
                GameEventMethod.GameAborted,
                -1,
                game.getStatus(),
                "Other player disconnected"
        );

        endGame();

        if (serverWorker1 == disconnectedClient) {
            serverWorker2.emit(gameAbortedEvent);
            serverWorker2.addToPool();

        } else if (serverWorker2 == disconnectedClient) {
            serverWorker1.emit(gameAbortedEvent);
            serverWorker1.addToPool();
        }
    }
}
