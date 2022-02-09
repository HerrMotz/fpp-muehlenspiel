package interfaces;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public record User(String id, String username) implements Serializable {
    @Serial
    private static final long serialVersionUID = 0L;

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId()) && Objects.equals(getUsername(), user.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUsername());
    }

    @Override
    public String toString() {
        return "  " + username + "  ";
    }

}
