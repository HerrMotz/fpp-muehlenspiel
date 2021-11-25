package backend;

import java.util.HashSet;

public class Game {
    private final Grid grid = new Grid();
    public static final int PLACE_PHASE = 0;
    public static final int MOVE_PHASE = 1;
    public static final int JUMP_PHASE = 2;
    public static final int GAME_OVER = 3;

    private int currentPhase = PLACE_PHASE;

    private boolean firstMoveByColour = Grid.COLOUR_BLACK;
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

    private void takeStoneFromInventory(boolean colour) {
        if (colour == Grid.COLOUR_WHITE && whiteStonesInInventory > 0) {
            whiteStonesInInventory--;
            whiteStonesOnTheGrid++;
            return;
        }

        if (colour == Grid.COLOUR_BLACK && blackStonesInInventory > 0) {
            blackStonesInInventory--;
            blackStonesOnTheGrid++;
        }

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

            if (
                stone.getColour() == firstMoveByColour
                && (firstMoveByColour == Grid.COLOUR_BLACK && blackStonesInInventory == 0)
                || (firstMoveByColour == Grid.COLOUR_WHITE && whiteStonesInInventory == 0)
            ) {
                currentPhase = MOVE_PHASE;
            }

            if (isInMill(posX, posY)) {
                thereIsAMill = true;
            }
        } else {
            throw new IllegalMoveException("You do not have any stones left");
        }
    }

    public boolean doesColorHavePossibleMoves(boolean colour) {
        for (int y = 0; y < Grid.LIMIT_Y; y++) {
            for (int x = 0; x < Grid.LIMIT_X; x++) {
                try {
                    Field field = grid.getField(x, y);
                    if (!field.isEmpty() && field.getStone().getColour() == colour) {
                        HashSet<Field> adjacentFields = grid.getAdjacentFields(x, y);
                        for (Field adjacentField : adjacentFields) {
                            if (adjacentField.isEmpty()) {
                                return true;
                            }
                        }
                    }
                } catch (IllegalMoveException ignored) {}
            }
        }
        return false;
    }

    public boolean areAllStonesInAMill(boolean colour) {
        for (int y = 0; y < Grid.LIMIT_Y; y++) {
            for (int x = 0; x < Grid.LIMIT_X; x++) {
                try {
                    Field field = grid.getField(x, y);
                    if (!field.isEmpty() && field.getStone().getColour() == colour) {
                        if (!isInMill(field.getPosX(), field.getPosY())) {
                            return false;
                        }
                    }
                } catch (IllegalMoveException ignored) {}
            }
        }
        return true;
    }

    public void moveStone(int posX, int posY, int toPosX, int toPosY) throws IllegalMoveException {
        if (thereIsAMill) {
            throw new IllegalMoveException("You have to remove a stone "+ this.getCurrentPlayer() +" before you can make another move.");
        }

        Field field = grid.getField(posX, posY);

        boolean colour = field.getStone().getColour();

        checkTurns(colour);

        if (!doesColorHavePossibleMoves(colour)) {
            currentPhase = GAME_OVER;
            return;
        }

        if (whiteInJumpPhase && field.getStone().getColour() == Grid.COLOUR_WHITE
                || blackInJumpPhase && field.getStone().getColour() == Grid.COLOUR_BLACK) {
            grid.jumpStone(posX, posY, toPosX, toPosY);
            changeTurns(colour);

        } else if (grid.areFieldsAdjacent(posX, posY, toPosX, toPosY)) {
            grid.moveStoneToAdjacentField(posX, posY, toPosX, toPosY);
            changeTurns(colour);

        } else {
            throw new IllegalMoveException("The fields are not adjacent to each other.");
        }

        if (isInMill(toPosX, toPosY)) {
            thereIsAMill = true;
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
                        blackStonesOnTheGrid--;
                        if (whiteStonesInInventory == 0) {
                            if (whiteStonesOnTheGrid <= 3) {
                                whiteInJumpPhase = true;
                            }
                            if (whiteStonesOnTheGrid < 3) {
                                currentPhase = GAME_OVER;
                            }
                        }
                    }
                    else {
                        blackStonesOnTheGrid--;
                        if (blackStonesInInventory == 0) {
                            if (blackStonesOnTheGrid <= 3) {
                                blackInJumpPhase = true;
                            }
                            if (blackStonesOnTheGrid < 3) {
                                currentPhase = GAME_OVER;
                            }
                        }
                    }
                    if (blackInJumpPhase && whiteInJumpPhase) currentPhase = JUMP_PHASE;
                } else {
                    throw new IllegalMoveException("This stone may not be removed.");
                }
            }
        } else {
            throw new IllegalMoveException("You may not remove a stone if you do not have a mill");
        }
    }

    public int getLimitX() {
        return Grid.LIMIT_X;
    }

    public int getLimitY() {
        return Grid.LIMIT_Y;
    }

    public void checkValidityOfFieldPosition(int posX, int posY) throws IllegalMoveException {
        grid.checkValidityOfFieldPosition(posX, posY);
    }

    public String getCurrentPlayer() {
        return lastMoveByColour ? "Black" : "White";
    }

    public String getOtherPlayer() {
        return lastMoveByColour ? "White" : "Black";
    }

    public String getPhaseAsString() {
        return switch (currentPhase) {
            case PLACE_PHASE -> "Place Phase";
            case MOVE_PHASE -> "Move Phase";
            case JUMP_PHASE -> "Jump Phase";
            case GAME_OVER -> "Game Over";
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

        for (HashSet<Field> mill : grid.getPossibleMills()) {
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

        return !isInMill(posX, posY) || areAllStonesInAMill(field.getStone().getColour());
    }

    @Override
    public String toString() {
        return grid.toString();
    }
}
