package gui;

import backend.Grid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class Stone {
    private final ImageIcon icon;
    private Point currentPoint;
    private Point previousPoint;

    private class ClickListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            previousPoint = e.getPoint();
        }
    }

    private class DragListener extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            Point dragEndPoint = e.getPoint();
            currentPoint.translate(
                    (int)(dragEndPoint.getX() - previousPoint.getX()),
                    (int)(dragEndPoint.getY() - previousPoint.getY())
            );
        }
    }

    public Stone(boolean colour, int xPos, int yPos) {
        if (colour == Grid.COLOUR_WHITE) {
            icon = new ImageIcon("resources/whiteStone60x60.png");
        } else {
            icon = new ImageIcon("resources/blackStone60x60.png");
        }

        currentPoint = new Point(xPos, yPos);
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

    public int getStoneHeight() {
        return icon.getIconHeight();
    }

    public int getStoneWidth() {
        return icon.getIconWidth();
    }
}
