package backend;

import java.util.Objects;

public class Field {
    private Stone stone;
    private final int posX;
    private final int posY;

    public Field(int posX, int posY) {
        stone = null;
        this.posX = posX;
        this.posY = posY;
    }

    public Stone getStone() {
        return stone;
    }

    public void setStone(Stone stone) {
        this.stone = stone;
    }

    public void removeStone() {
        this.stone = null;
    }

    public boolean empty() {
        return stone == null;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return posX == field.posX && posY == field.posY && Objects.equals(getStone(), field.getStone());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStone(), posX, posY);
    }
}
