package gui;

import backend.Grid;

import javax.swing.*;
import java.awt.*;

public class Stone {
    private final ImageIcon icon;
    private Point currentPoint;
    private Point previousPoint;

    private Point dragStartPoint;
    private boolean isBeingDragged = false;

    private final boolean colour;

    public Stone(boolean colour, int xPos, int yPos) {
        this.colour = colour;
        if (colour == Grid.COLOUR_WHITE) {
            icon = new ImageIcon("resources/whiteStone60x60.png");
        } else {
            icon = new ImageIcon("resources/blackStone60x60.png");
        }

        currentPoint = new Point(xPos, yPos);
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
        this.getCurrentPoint().translate(
                (int)(xPos - this.getPreviousPoint().getX()),
                (int)(yPos - this.getPreviousPoint().getY())
        );
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
}
