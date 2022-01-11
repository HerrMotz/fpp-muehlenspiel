package backend.logic;

import interfaces.StoneInterface;

import java.io.Serializable;
import java.util.Objects;

public class Stone implements StoneInterface, Serializable {

    boolean colour;

    public Stone(boolean colour) {
        this.colour = colour;
    }

    public boolean getColour() {
        return colour;
    }

    @Override
    public String toString() {
        return "Stone{" +
                "colour=" + (colour?"White":"Black") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stone stone = (Stone) o;
        return getColour() == stone.getColour();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getColour());
    }
}
