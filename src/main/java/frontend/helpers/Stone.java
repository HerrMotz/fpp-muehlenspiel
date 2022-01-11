package frontend.helpers;

import interfaces.GameInterface;
import interfaces.StoneInterface;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class Stone implements StoneInterface, Serializable {
    private final ImageIcon icon;
    private Point currentPoint;
    private Point previousPoint;

    private Point dragStartPoint;
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
        previousPoint = new Point(xPos, yPos);
    }

    public boolean contains(Point point) {
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
        this.getCurrentPoint().move(xPos, yPos);
        this.setPreviousPoint(this.getCurrentPoint());
    }

    public void moveToCenter(int xPos, int yPos) {
        moveToTopLeftCorner(xPos - this.getStoneWidth()/2, yPos - this.getStoneHeight()/2);
    }

    public boolean hasBeenDragged() {
        return previousPoint == null;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public Point getCurrentPoint() {
        return currentPoint;
    }

    public Point getPreviousPoint() {
        return previousPoint;
    }

    public void setPreviousPoint(Point previousPoint) {
        this.previousPoint = previousPoint;
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
        this.dragStartPoint = dragStartPoint;
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

    public void setGridPosX(int gridPosX) {
        this.gridPosX = gridPosX;
    }

    public int getGridPosY() {
        return gridPosY;
    }

    public void setGridPosY(int gridPosY) {
        moveToCenter(currentPoint.x, gridPosY);
        this.gridPosY = gridPosY;
    }
}
