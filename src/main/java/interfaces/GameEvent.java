package interfaces;

import java.io.Serializable;

public class GameEvent implements Serializable {
    private final GameEventMethod method;
    private final Object[] arguments;

    public GameEvent(GameEventMethod method, Object... arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    public GameEventMethod getMethod() {
        return method;
    }

    public Object[] getArguments() {
        return arguments;
    }
}
