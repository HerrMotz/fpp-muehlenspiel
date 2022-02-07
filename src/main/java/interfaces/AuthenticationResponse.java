package interfaces;

import java.io.Serial;
import java.io.Serializable;

public class AuthenticationResponse implements Serializable {
    private final String message;
    private final boolean success;
    private final AuthenticationResponseMethod authenticationResponseMethod;
    private final User user;

    public AuthenticationResponse(String message, boolean success, AuthenticationResponseMethod method, User user) {
        this.message = message;
        this.success = success;
        this.authenticationResponseMethod = method;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public AuthenticationResponseMethod getMethod() { return authenticationResponseMethod; }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "AuthenticationResponse{" +
                "message='" + message + '\'' +
                ", success=" + success +
                ", authenticationResponseMethod=" + authenticationResponseMethod +
                ", user=" + user +
                '}';
    }
}
