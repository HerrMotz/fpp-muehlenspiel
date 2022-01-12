package frontend.helpers;

import interfaces.GameInterface;
import interfaces.StoneInterface;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class Stone implements StoneInterface, Serializable {
    private final ImageIcon icon;
    private final Point currentPoint;

    private final Point dragStartPoint;
    private boolean isBeingDragged = false;

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

        currentPoint = new Point(xPos, yPos);
        dragStartPoint = currentPoint;
    }

    public boolean contains(Point point) {
        System.out.println(currentPoint);

        Point bottomRightPoint = new Point(
                (int)(currentPoint.getX() + getIcon().getIconWidth()),
                (int)currentPoint.getY() + getIcon().getIconHeight()
        );

        return point.getX() >= currentPoint.getX() &&
                point.getY() >= currentPoint.getY() &&
                point.getX() <= bottomRightPoint.getX() &&
                point.getY() <= bottomRightPoint.getY();
    }

    public void moveToTopLeftCorner(int xPos, int yPos) {
        this.getCurrentPoint().setLocation(xPos, yPos);
    }

    public void moveToCenter(int xPos, int yPos) {
        moveToTopLeftCorner(xPos - this.getStoneWidth()/2, yPos - this.getStoneHeight()/2);
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public Point getCurrentPoint() {
        return currentPoint;
    }

    public int getStoneHeight() {
        return icon.getIconHeight();
    }

    public int getStoneWidth() {
        return icon.getIconWidth();
    }

    public Point getDragStartPoint() {
        return dragStartPoint;
    }

    public void setDragStartPoint(Point dragStartPoint) {
        this.dragStartPoint.setLocation(dragStartPoint);
    }

    public boolean isBeingDragged() {
        return isBeingDragged;
    }

    public void setBeingDragged(boolean beingDragged) {
        isBeingDragged = beingDragged;
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

    public void setGridPositions(int gridPosX, int gridPosY) {
        this.gridPosX = gridPosX;
        this.gridPosY = gridPosY;
        moveToCenter(100 + 100 * gridPosX, 100 + 100 * gridPosY);
    }

    public void resetToDragStart() {
        moveToTopLeftCorner(getDragStartPoint().x, getDragStartPoint().y);
        setBeingDragged(false);
    }
}
