package backend;

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

    public boolean isEmpty() {
        return stone == null;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    @Override
    public String toString() {
        return "Field{" +
                "stone=" + stone +
                ", posX=" + posX +
                ", posY=" + posY +
                '}';
    }
}
