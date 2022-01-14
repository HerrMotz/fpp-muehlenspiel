package interfaces;

import java.io.Serializable;

public class GameEvent implements Serializable {
    private final GameEventMethod method;
    private final int reference;
    private final GameStatus gameStatus;
    private final Object[] arguments;

    public GameEvent(GameEventMethod method, int reference, GameStatus gameStatus, Object... arguments) {
        this.method = method;
        this.reference = reference;
        this.gameStatus = gameStatus;
        this.arguments = arguments;
    }

    public GameEventMethod getMethod() {
        return method;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public int getReference() {
        return reference;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }
}
