package frontend.helpers;

import frontend.panels.GamePanel;
import interfaces.GameInterface;
import interfaces.StoneInterface;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class Stone implements StoneInterface, Serializable {
    private final ImageIcon icon;
    private final Point point;

    private volatile Point dragStartPoint;

    private final boolean colour;

    private int gridPosX;
    private int gridPosY;

    public Stone(boolean colour, int xPos, int yPos) {
        this.colour = colour;

        if (colour == GameInterface.COLOUR_WHITE) {
            icon = new ImageIcon("resources/whiteStone60x60.png");
        } else {
            icon = new ImageIcon("resources/blackStone60x60.png");
        }

        point = new Point(xPos, yPos);
        dragStartPoint = point;
    }

    public boolean contains(Point point) {
        Point bottomRightPoint = new Point(
                (int)(this.point.getX() + getIcon().getIconWidth()),
                (int) this.point.getY() + getIcon().getIconHeight()
        );

        return point.getX() >= this.point.getX() &&
                point.getY() >= this.point.getY() &&
                point.getX() <= bottomRightPoint.getX() &&
                point.getY() <= bottomRightPoint.getY();
    }

    public void moveToTopLeftCorner(int xPos, int yPos) {
        point.setLocation(xPos, yPos);
    }

    public void moveToCenter(int xPos, int yPos) {
        moveToTopLeftCorner(xPos - this.getStoneWidth()/2, yPos - this.getStoneHeight()/2);
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public Point getPoint() {
        return point;
    }

    public int getStoneHeight() {
        return icon.getIconHeight();
    }

    public int getStoneWidth() {
        return icon.getIconWidth();
    }

    public synchronized void setDragStartPoint(Point dragStartPoint) {
        this.dragStartPoint = dragStartPoint;
    }

    public synchronized void resetToDragStart() {
        moveToTopLeftCorner(dragStartPoint.x, dragStartPoint.y);
    }

    public boolean getColour() {
        return colour;
    }

    public int getGridPosX() {
        return gridPosX;
    }

    public int getGridPosY() {
        return gridPosY;
    }

    public synchronized void setGridPosition(int gridPosX, int gridPosY) {
        this.gridPosX = gridPosX;
        this.gridPosY = gridPosY;
        moveToCenter(
        GamePanel.gridStart + GamePanel.distanceBetweenGridLines * gridPosX,
        GamePanel.gridStart + GamePanel.distanceBetweenGridLines * gridPosY);
        setDragStartPoint(point);
    }
}
