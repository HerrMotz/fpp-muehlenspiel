package frontend.helpers;

import frontend.Client;
import interfaces.*;

import java.io.IOException;

public class Game {
    private final Client client;

    public static final int LIMIT_X = 7;
    public static final int LIMIT_Y = 7;

    private GamePhase currentPhase;

    private boolean thereIsAMill = false;

    private int whiteStonesInInventory = 9;
    private int blackStonesInInventory = 9;

    private int whiteStonesOnTheGrid = 0;
    private int blackStonesOnTheGrid = 0;

    private boolean whiteInJumpPhase = false;
    private boolean blackInJumpPhase = false;

    private boolean lastMoveByColour;
    private boolean myColour;

    public Game(Client client) {
        this.client = client;
        currentPhase = GamePhase.WAITING_FOR_PLAYERS;
    }

    public void startGame(boolean myColour, boolean startPlayer) {
        this.myColour = myColour;
        lastMoveByColour = !startPlayer;
        currentPhase = GamePhase.PLACE_PHASE;
    }

    public void abortGame() {
        currentPhase = GamePhase.ABORTED;
    }

    public void swapMoves() { lastMoveByColour = !lastMoveByColour; }

    public boolean getMyColour() {
        return myColour;
    }

    public String getMyColourAsString() {
        if (myColour == GameInterface.COLOUR_WHITE) {
            return "White";
        } else {
            return "Black";
        }
    }

    public void placeStone(int reference, int posX, int posY, boolean colour) throws IllegalMoveException, IOException {
        if (thereIsAMill) {
            throw new IllegalMoveException("You have to remove a stone " + this.getCurrentPlayerAsString());
        }

        client.emit(new GameEvent(
                GameEventMethod.PlaceStone,
                reference,
                null,
                posX,
                posY,
                colour
        ));
    }

    public void removeStone(int reference, int posX, int posY) throws IllegalMoveException, IOException {
        if (!thereIsAMill) {
            throw new IllegalMoveException("There is no mill, so you may not remove a stone");
        }

        client.emit(new GameEvent(
                GameEventMethod.RemoveStone,
                reference,
                null,
                posX,
                posY
        ));
    }

    public void moveStone(int reference, int posX, int posY, int toPosX, int toPosY) throws IllegalMoveException, IOException {
        if (thereIsAMill) {
            throw new IllegalMoveException("You have to remove a stone " + this.getCurrentPlayerAsString() + " before you can make another move.");
        }

        client.emit(new GameEvent(
                GameEventMethod.MoveStone,
                reference,
                null,
                posX,
                posY,
                toPosX,
                toPosY
        ));
    }

    public Client getClient() {
        return client;
    }

    public boolean isThereAMill() {
        return thereIsAMill;
    }

    public int getLimitX() {
        return LIMIT_X;
    }

    public int getLimitY() {
        return LIMIT_Y;
    }

    public boolean isFieldPositionValid(int posX, int posY) {
        if (posX >= LIMIT_X || posY >= LIMIT_Y || posX < 0 || posY < 0) {
            return false;
        }

        if (posX > Math.floorDiv(LIMIT_X, 2))
            posX = (LIMIT_X - 1 - posX) % (LIMIT_X - 1);

        if (posY > Math.floorDiv(LIMIT_Y, 2))
            posY = (LIMIT_Y - 1 - posY) % (LIMIT_Y - 1);

        return (posX != 0 || (posY != 1 && posY != 2)) &&
                (posX != 1 || (posY != 0 && posY != 2)) &&
                (posX != 2 || (posY != 0 && posY != 1)) &&
                (posX != 3 || posY != 3);
    }

    public GamePhase getPhase() {
        return currentPhase;
    }

    public String getPhaseAsString() {
        return switch (getPhase()) {
            case GAME_OVER -> "Game Over!";
            case ABORTED -> "Game aborted.";
            case PLACE_PHASE -> "Place Phase";
            case MOVE_PHASE -> "Move Phase";
            case JUMP_PHASE -> "Jump Phase";
            case WAITING_FOR_PLAYERS -> "Waiting for another player to join...";
        };
    }

    public boolean isItMyTurn() {
        return !lastMoveByColour == myColour;
    }

    public String getCurrentPlayerAsString() {
        return !lastMoveByColour == GameInterface.COLOUR_WHITE ? "White" : "Black";
    }

    public String getOtherPlayerAsString() {
        return lastMoveByColour == GameInterface.COLOUR_WHITE ? "White" : "Black";
    }

    public Integer getStonesInInventory(boolean colour) {
        if (colour == GameInterface.COLOUR_WHITE) return whiteStonesInInventory;
        if (colour == GameInterface.COLOUR_BLACK) return blackStonesInInventory;
        throw new IllegalArgumentException("The given colour does not exist");
    }

    public Integer getStonesOnGrid(boolean colour) {
        if (colour == GameInterface.COLOUR_WHITE) return whiteStonesOnTheGrid;
        if (colour == GameInterface.COLOUR_BLACK) return blackStonesOnTheGrid;
        throw new IllegalArgumentException("The given colour does not exist");
    }

    public Boolean isColourInJumpPhase(boolean colour) {
        if (colour == GameInterface.COLOUR_WHITE) return whiteInJumpPhase;
        if (colour == GameInterface.COLOUR_BLACK) return blackInJumpPhase;
        throw new IllegalArgumentException("The given colour does not exist");
    }

    public void setStatus(GameStatus gameStatus) {
        if (gameStatus != null) {
            currentPhase = gameStatus.currentPhase;

            thereIsAMill = gameStatus.isThereAMill;

            whiteStonesInInventory = gameStatus.whiteStonesInInventory;
            blackStonesInInventory = gameStatus.blackStonesInInventory;

            whiteStonesOnTheGrid = gameStatus.whiteStonesOnTheGrid;
            blackStonesOnTheGrid = gameStatus.blackStonesOnTheGrid;

            whiteInJumpPhase = gameStatus.whiteInJumpPhase;
            blackInJumpPhase = gameStatus.blackInJumpPhase;
        }
    }
}
