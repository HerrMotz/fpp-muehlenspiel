package interfaces;

public interface StoneInterface {
    boolean getColour();

    @Override
    String toString();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}
