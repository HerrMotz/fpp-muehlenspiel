package backend.logic;

import interfaces.*;

import java.util.HashSet;

public class Game {
    private final Grid grid = new Grid();

    private GamePhase currentPhase = GamePhase.PLACE_PHASE;

    private boolean firstMoveByColour;
    private boolean lastMoveByColour;

    private boolean thereIsAMill = false;

    private int whiteStonesInInventory = 9;
    private int blackStonesInInventory = 9;

    private int whiteStonesOnTheGrid = 0;
    private int blackStonesOnTheGrid = 0;

    private boolean whiteInJumpPhase = false;
    private boolean blackInJumpPhase = false;

    public Game(boolean random, double x) throws IllegalMoveException {
        grid.generateMills();
        if (random) {
            firstMoveByColour = (Math.random() < x);
            lastMoveByColour = firstMoveByColour;
        }
    }

    public synchronized Integer getStonesInInventory(boolean colour) {
        if (colour == GameInterface.COLOUR_WHITE) return whiteStonesInInventory;
        if (colour == GameInterface.COLOUR_BLACK) return blackStonesInInventory;
        throw new IllegalArgumentException("The given colour does not exist");
    }

    private synchronized void takeStoneFromInventory(boolean colour) {
        if (colour == GameInterface.COLOUR_WHITE && whiteStonesInInventory > 0) {
            whiteStonesInInventory--;
            whiteStonesOnTheGrid++;
            return;
        }

        if (colour == GameInterface.COLOUR_BLACK && blackStonesInInventory > 0) {
            blackStonesInInventory--;
            blackStonesOnTheGrid++;
        }

    }

    private synchronized void checkTurns(boolean moveByColour) throws IllegalMoveException {
        if (moveByColour == lastMoveByColour)
            throw new IllegalMoveException("It's the other player's turn.");
    }

    private synchronized  void changeTurns() {
        lastMoveByColour = !lastMoveByColour;
    }

    public synchronized void placeStone(int posX, int posY, StoneInterface stone) throws IllegalMoveException {
        if (thereIsAMill) {
            throw new IllegalMoveException("You have to remove a stone "+ this.getCurrentPlayer() +" before you can make another move.");
        }

        if (currentPhase != GamePhase.PLACE_PHASE) {
            throw new IllegalMoveException("The game is currently not in the place phase.");
        }

        boolean colour = stone.getColour();
        checkTurns(colour);

        if (getStonesInInventory(colour) > 0) {
            grid.placeStone(posX, posY, stone);
            takeStoneFromInventory(colour);
            changeTurns();

            if (colour == firstMoveByColour
                && ((firstMoveByColour == GameInterface.COLOUR_BLACK && blackStonesInInventory == 0)
                    || (firstMoveByColour == GameInterface.COLOUR_WHITE && whiteStonesInInventory == 0)
                )) {
                currentPhase = GamePhase.MOVE_PHASE;
            }

            if (isInMill(posX, posY)) {
                thereIsAMill = true;
            }
        } else {
            throw new IllegalMoveException("You do not have any stones left");
        }
    }

    public synchronized void placeStoneCheckTurn(boolean moveByColour, int posX, int posY, StoneInterface stone) throws IllegalMoveException {
        checkTurns(moveByColour);
        placeStone(posX, posY, stone);
    }

    public synchronized boolean doesColorHavePossibleMoves(boolean colour) {
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

    public synchronized boolean areAllStonesInAMill(boolean colour) {
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

    public synchronized void moveStone(int posX, int posY, int toPosX, int toPosY) throws IllegalMoveException {
        if (thereIsAMill) {
            throw new IllegalMoveException("You have to remove a stone "+ this.getCurrentPlayer() +" before you can make another move.");
        }

        Field field = grid.getField(posX, posY);

        boolean colour = field.getStone().getColour();

        checkTurns(colour);

        if (!doesColorHavePossibleMoves(colour)) {
            currentPhase = GamePhase.GAME_OVER;
            return;
        }

        if (whiteInJumpPhase && field.getStone().getColour() == GameInterface.COLOUR_WHITE
                || blackInJumpPhase && field.getStone().getColour() == GameInterface.COLOUR_BLACK) {
            grid.jumpStone(posX, posY, toPosX, toPosY);
            changeTurns();

        } else if (grid.areFieldsAdjacent(posX, posY, toPosX, toPosY)) {
            grid.moveStoneToAdjacentField(posX, posY, toPosX, toPosY);
            changeTurns();

        } else {
            throw new IllegalMoveException("The fields are not adjacent to each other.");
        }

        if (isInMill(toPosX, toPosY)) {
            thereIsAMill = true;
        }
    }

    public synchronized void moveStoneCheckTurn(boolean moveByColour, int posX, int posY, int toPosX, int toPosY) throws IllegalMoveException {
        checkTurns(moveByColour);
        moveStone(posX, posY, toPosX, toPosY);
    }

    public synchronized void removeStone(int posX, int posY) throws IllegalMoveException {
        if (thereIsAMill) {
            Field field = grid.getField(posX, posY);
            System.out.println(field.getStone().getColour());
            if (field.getStone().getColour() == lastMoveByColour) {
                throw new IllegalMoveException("You may not remove one of your own stones");
            } else {
                if (isStoneLegalToRemove(posX, posY)) {
                    boolean colour = field.getStone().getColour();
                    grid.removeStone(posX, posY);
                    thereIsAMill = false;

                    // TODO rewrite this as a method
                    if (colour == GameInterface.COLOUR_WHITE) {
                        whiteStonesOnTheGrid--; // wtf
                        if (whiteStonesInInventory == 0) {
                            if (whiteStonesOnTheGrid <= 3) {
                                whiteInJumpPhase = true;
                            }
                            if (whiteStonesOnTheGrid < 3) {
                                currentPhase = GamePhase.GAME_OVER;
                            }
                        }
                    } else {
                        blackStonesOnTheGrid--;
                        if (blackStonesInInventory == 0) {
                            if (blackStonesOnTheGrid <= 3) {
                                blackInJumpPhase = true;
                            }
                            if (blackStonesOnTheGrid < 3) {
                                currentPhase = GamePhase.GAME_OVER;
                            }
                        }
                    }
                } else {
                    throw new IllegalMoveException("This stone may not be removed.");
                }
            }
        } else {
            throw new IllegalMoveException("You may not remove a stone if you do not have a mill");
        }
    }

    public synchronized void removeStoneCheckTurn(boolean moveByColour, int posX, int posY) throws IllegalMoveException {
        checkTurns(!moveByColour);
        removeStone(posX, posY);
    }

    public synchronized boolean getCurrentPlayer() {
        return !lastMoveByColour;
    }

    public synchronized GamePhase getPhase() {
        return currentPhase;
    }

    public synchronized boolean isInMill(int posX, int posY) {
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

    public synchronized boolean isThereAMill() {
        return thereIsAMill;
    }

    public synchronized boolean isStoneLegalToRemove(int posX, int posY) throws IllegalMoveException {
        Field field;
        field = grid.getField(posX, posY);
        if (field.isEmpty()) throw new IllegalMoveException("There is no stone at the given field, which may be removed");

        return !isInMill(posX, posY) || areAllStonesInAMill(field.getStone().getColour());
    }

    public synchronized GameStatus getStatus() {
        return new GameStatus(
                getPhase(),
                isThereAMill(),
                whiteStonesInInventory,
                blackStonesInInventory,
                whiteStonesOnTheGrid,
                blackStonesOnTheGrid,
                whiteInJumpPhase,
                blackInJumpPhase
        );
    }

    @Override
    public String toString() {
        return grid.toString();
    }
}
