package backend;

import java.util.HashSet;
import java.util.Set;

public class Game {
    private final Grid grid = new Grid(7, 7);
    public static final int PLACE_PHASE = 0;
    public static final int MOVE_PHASE = 1;
    public static final int JUMP_PHASE = 2;

    @SuppressWarnings("FieldMayBeFinal")
    private int currentPhase = PLACE_PHASE;

    private boolean lastMoveByColour = Grid.COLOUR_BLACK;

    private int whiteStoneCounter = 9;
    private int blackStoneCounter = 9;

    public int getStonesInInventory(boolean colour) {
        if (colour == Grid.COLOUR_WHITE) return whiteStoneCounter;
        if (colour == Grid.COLOUR_BLACK) return blackStoneCounter;
        throw new IllegalArgumentException("The given colour does not exist");
    }

    private boolean takeStoneFromInventory(boolean colour) {
        if (colour == Grid.COLOUR_WHITE && whiteStoneCounter > 0) {
            whiteStoneCounter--;
            return true;
        }

        if (colour == Grid.COLOUR_BLACK && blackStoneCounter > 0) {
            blackStoneCounter--;
            return true;
        }

        return false;
    }

    private void checkTurns(boolean moveByColour) throws IllegalMoveException {
        if (moveByColour == lastMoveByColour) throw new IllegalMoveException("It's the other player's turn.");
    }

    private void changeTurns(boolean moveByColour) {
        lastMoveByColour = moveByColour;
    }

    public void placeStone(int posX, int posY, Stone stone) throws IllegalMoveException {
        if (currentPhase != PLACE_PHASE) {
            throw new IllegalMoveException("The game is currently not in the place phase.");
        }

        checkTurns(stone.getColour());

        if (getStonesInInventory(stone.getColour()) > 0) {
            grid.placeStone(posX, posY, stone);
            takeStoneFromInventory(stone.getColour());
            changeTurns(stone.getColour());

            if (stone.getColour() == Grid.COLOUR_BLACK && blackStoneCounter == 0) {
                currentPhase = MOVE_PHASE;
            }
        } else {
            throw new IllegalMoveException("You do not have any stones left");
        }
    }

    public void moveStone(int posX, int posY, int toPosX, int toPosY) throws IllegalMoveException {
        if (currentPhase != JUMP_PHASE) {
            if (currentPhase != MOVE_PHASE) {
                throw new IllegalMoveException("You cannot move any stones in this stage of the game");
            } else {
                if (!grid.areFieldsAdjacent(posX, posY, toPosX, toPosY)) {
                    throw new IllegalMoveException("The fields are not adjacent to each other.");
                } else {
                    Field field = grid.getField(posX, posY);
                    boolean colour = field.getStone().getColour();
                    checkTurns(colour);
                    grid.moveStoneToAdjacentField(posX, posY, toPosX, toPosY);
                    changeTurns(colour);
                }
            }
        } else {
            Field field = grid.getField(posX, posY);
            checkTurns(field.getStone().getColour());
            grid.jumpStone(posX, posY, toPosX, toPosY);
            changeTurns(field.getStone().getColour());
        }
    }

    public int getLimitX() {
        return grid.LIMIT_X;
    }

    public int getLimitY() {
        return grid.LIMIT_Y;
    }

    public void checkValidityOfFieldPosition(int posX, int posY) throws IllegalMoveException {
        grid.checkValidityOfFieldPosition(posX, posY);
    }

    public String getCurrentPlayer() {
        return lastMoveByColour ? "Black" : "White";
    }

    public String getPhase() {
        return switch (currentPhase) {
            case PLACE_PHASE -> "Place Phase";
            case MOVE_PHASE -> "Move Phase";
            case JUMP_PHASE -> "Jump Phase";
            default -> throw new IllegalStateException("Unexpected value: " + currentPhase);
        };
    }

    public boolean checkMill(int posX, int posY) {
        Field field;
        try {
            field = grid.getField(posX, posY);
        } catch (IllegalMoveException e) {
            return false;
        }

        if (field.getStone() == null) return false;

        Set<Field> adjFields;
        try {
            adjFields = grid.getAdjacentFields(posX, posY);
        } catch (IllegalMoveException e) {
            return false;
        }

        int countAdjacentFields = 0;

        for (Field adjField : adjFields) {
            if (!adjField.empty() && adjField.getStone().getColour() == field.getStone().getColour()) {
                int xDirection = (field.getPosX() - adjField.getPosX()) % 2;
                int yDirection = (field.getPosY() - adjField.getPosY()) % 2;

                try {
                    Set<Field> adj2Fields = grid.getAdjacentFields(field.getPosX(), field.getPosY());
                    for (Field adj2Field : adj2Fields) {
                        int x2Direction = (field.getPosX() - adj2Field.getPosX()) % 2;
                        int y2Direction = (field.getPosY() - adj2Field.getPosY()) % 2;
                        if (xDirection == x2Direction || yDirection == y2Direction) {
                            Set<Field> adj3Fields = grid.getAdjacentFields(field.getPosX(), field.getPosY());
                            for (Field adj3Field : adj3Fields) {
                                int x3Direction = (field.getPosX() - adj3Field.getPosX()) % 2;
                                int y3Direction = (field.getPosY() - adj3Field.getPosY()) % 2;
                                if (xDirection == x3Direction || yDirection == y3Direction) {
                                    return true;
                                }
                            }
                        }
                    }
                } catch (IllegalMoveException e) {
                    return false;
                }
            }
        }

        return false;
    }

    public boolean isStoneLegalToRemove(int posX, int posY) {

        return false;
    }

    @Override
    public String toString() {
        return grid.toString();
    }
}
