package backend;

import java.util.Objects;

public class Stone {

    boolean colour;

    public Stone(boolean colour) {
        this.colour = colour;
    }

    public boolean getColour() {
        return colour;
    }

    public void setColour(boolean colour) {
        this.colour = colour;
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