package frontend.helpers;

import frontend.panels.GamePanel;

import java.awt.*;

public class FieldPosition extends Point {
    int gridX;
    int gridY;

    public FieldPosition(int x, int y) {
        super(
        GamePanel.gridStart + x * GamePanel.distanceBetweenGridLines,
        GamePanel.gridStart + y * GamePanel.distanceBetweenGridLines
        );
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
