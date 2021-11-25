package backend;

import java.util.Set;

public class Game {
    private final Grid grid = new Grid(7, 7);
    public static final int PLACE_PHASE = 0;
    public static final int MOVE_PHASE = 1;
    public static final int JUMP_PHASE = 2;

    @SuppressWarnings("FieldMayBeFinal")
    private int currentPhase = PLACE_PHASE;

    private boolean lastMoveByColour = Grid.COLOUR_BLACK;

    private boolean thereIsAMill = false;

    private int whiteStonesInInventory = 9;
    private int blackStonesInInventory = 9;

    private int whiteStonesOnTheGrid = 0;
    private int blackStonesOnTheGrid = 0;

    private boolean whiteInJumpPhase = false;
    private boolean blackInJumpPhase = false;

    public Game() throws IllegalMoveException {
        grid.generateMills();
    }

    public int getStonesInInventory(boolean colour) {
        if (colour == Grid.COLOUR_WHITE) return whiteStonesInInventory;
        if (colour == Grid.COLOUR_BLACK) return blackStonesInInventory;
        throw new IllegalArgumentException("The given colour does not exist");
    }

    private boolean takeStoneFromInventory(boolean colour) {
        if (colour == Grid.COLOUR_WHITE && whiteStonesInInventory > 0) {
            whiteStonesInInventory--;
            whiteStonesOnTheGrid++;
            return true;
        }

        if (colour == Grid.COLOUR_BLACK && blackStonesInInventory > 0) {
            blackStonesInInventory--;
            blackStonesOnTheGrid++;
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
        if (thereIsAMill) {
            throw new IllegalMoveException("You have to remove a stone "+ this.getCurrentPlayer() +" before you can make another move.");
        }
        if (currentPhase != PLACE_PHASE) {
            throw new IllegalMoveException("The game is currently not in the place phase.");
        }

        checkTurns(stone.getColour());


        if (getStonesInInventory(stone.getColour()) > 0) {
            grid.placeStone(posX, posY, stone);
            takeStoneFromInventory(stone.getColour());
            changeTurns(stone.getColour());

            if (stone.getColour() == Grid.COLOUR_BLACK && blackStonesInInventory == 0) {
                currentPhase = MOVE_PHASE;
            }

            if (isInMill(posX, posY)) {
                thereIsAMill = true;
                System.out.println("there is a mill");
            }
        } else {
            throw new IllegalMoveException("You do not have any stones left");
        }
    }

    public void moveStone(int posX, int posY, int toPosX, int toPosY) throws IllegalMoveException {
        if (thereIsAMill) {
            throw new IllegalMoveException("You have to remove a stone "+ this.getCurrentPlayer() +" before you can make another move.");
        }
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
                    if (isInMill(toPosX, toPosY)) {
                        thereIsAMill = true;
                        System.out.println("there is a mill");
                    }
                }
            }
        } else {
            Field field = grid.getField(posX, posY);
            checkTurns(field.getStone().getColour());
            grid.jumpStone(posX, posY, toPosX, toPosY);
            changeTurns(field.getStone().getColour());
        }
    }

    public void removeStone(int posX, int posY) throws IllegalMoveException {
        if (thereIsAMill) {
            Field field = grid.getField(posX, posY);
            if (field.getStone().getColour() == lastMoveByColour) {
                throw new IllegalMoveException("You may not remove one of your own stones");
            } else {
                if (isStoneLegalToRemove(posX, posY)) {
                    boolean colour = field.getStone().getColour();
                    grid.removeStone(posX, posY);
                    thereIsAMill = false;
                    if (colour == Grid.COLOUR_WHITE) {
                        whiteStonesOnTheGrid--;
                    }
                    else {
                        blackStonesOnTheGrid--;
                    }
                } else {
                    throw new IllegalMoveException("This stone may not be removed.");
                }
            }
        } else {
            throw new IllegalMoveException("You may not remove a stone if you do not have a mill");
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

    public String getPhaseAsString() {
        return switch (currentPhase) {
            case PLACE_PHASE -> "Place Phase";
            case MOVE_PHASE -> "Move Phase";
            case JUMP_PHASE -> "Jump Phase";
            default -> throw new IllegalStateException("Unexpected value: " + currentPhase);
        };
    }

    public int getPhase() {
        return currentPhase;
    }

    public boolean isInMill(int posX, int posY) {
        Field field;
        try {
            field = grid.getField(posX, posY);
        } catch (IllegalMoveException e) {
            return false;
        }

        if (field.isEmpty()) return false;

        for (Set<Field> mill : grid.getPossibleMills()) {
            if (mill.contains(field)) {
                boolean allTheSameColour = true;
                for (Field millField : mill) {
                    if (millField.isEmpty() || millField.getStone().getColour() != field.getStone().getColour()) {
                        allTheSameColour = false;
                        break;
                    }
                }
                if (allTheSameColour) return true;
            }
        }
        return false;
    }

    public boolean isThereAMill() {
        return thereIsAMill;
    }

    public boolean isStoneLegalToRemove(int posX, int posY) throws IllegalMoveException {
        Field field;
        field = grid.getField(posX, posY);
        if (field.isEmpty()) throw new IllegalMoveException("There is no stone at the given field, which may be removed");

        boolean moreThanThreeStonesLeft = true;
        if (field.getStone().getColour() == Grid.COLOUR_WHITE) {
            if (whiteStonesOnTheGrid <= 3) {
                moreThanThreeStonesLeft = false;
            }
        } else if (blackStonesOnTheGrid <= 3) {
            moreThanThreeStonesLeft = false;
        }

        return (isInMill(posX, posY) && !moreThanThreeStonesLeft)
                || !isInMill(posX, posY);
    }

    @Override
    public String toString() {
        return grid.toString();
    }
}
