package frontend.helpers;

import java.awt.*;

public class FieldPosition extends Point {
    int gridX;
    int gridY;

    public FieldPosition(int x, int y) {
        super(100 + x*100, 100 + y*100);
        gridX = x;
        gridY = y;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }
}
