package interfaces;

import java.io.Serializable;

public record GameEvent(GameEventMethod method, int reference, GameStatus gameStatus,
                        Object... arguments) implements Serializable {

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
