package frontend.helpers;

import interfaces.GameInterface;
import interfaces.StoneInterface;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class Stone implements StoneInterface, Serializable {
    private final ImageIcon icon;
    private final Point point;

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

    public void setDragStartPoint(Point dragStartPoint) {
        System.out.println("Set Drag Start Point. Is being dragged: " + isBeingDragged);
        if (!isBeingDragged) {
            System.out.println("Set drag start point " + dragStartPoint);
            this.dragStartPoint = dragStartPoint;
            System.out.println(dragStartPoint);
            isBeingDragged = true;
        }
    }

    public void resetToDragStart() {
        System.out.println("reset to drag start x:" + dragStartPoint.getX() + " y:" + dragStartPoint.getY());
        moveToTopLeftCorner(dragStartPoint.x, dragStartPoint.y);
        isBeingDragged = false;
    }

    public void resetForNewDrag() {
        isBeingDragged = false;
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

    public void setGridPosition(int gridPosX, int gridPosY) {
        System.out.println("setGridPosition x:" + gridPosX + " y:" + gridPosY);
        this.gridPosX = gridPosX;
        this.gridPosY = gridPosY;
        moveToCenter(100 + 100 * gridPosX, 100 + 100 * gridPosY);
        this.dragStartPoint = point;
        resetForNewDrag();
    }
}
