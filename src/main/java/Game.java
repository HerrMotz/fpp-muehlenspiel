public class Game {
    Grid grid = new Grid(7, 7);
    public static final int PLACE_PHASE = 0;
    public static final int MOVE_PHASE = 1;
    public static final int JUMP_PHASE = 2;

    @SuppressWarnings("FieldMayBeFinal")
    private int currentPhase = PLACE_PHASE;

    private boolean lastMoveByColour = Grid.COLOUR_BLACK;

    private void checkTurns(boolean moveByColour) throws IllegalMoveException {
        if (moveByColour == lastMoveByColour) throw new IllegalMoveException("It's the other player's turn.");
        else lastMoveByColour = moveByColour;
    }

    public void placeStone(int posX, int posY, boolean colour) throws IllegalMoveException {
        if (currentPhase != PLACE_PHASE) {
            throw new IllegalMoveException("The game is currently not in the place phase.");
        }
        checkTurns(colour);
        grid.placeStone(posX, posY, colour);
    }

    public void moveStone(int posX, int posY, int toPosX, int toPosY) throws IllegalMoveException {
        if (currentPhase != JUMP_PHASE) {
            if (currentPhase != MOVE_PHASE) {
                throw new IllegalMoveException("You cannot move any stones in this stage of the game");
            } else {
                if (!grid.areFieldsAdjacent(posX, posY, toPosX, toPosY)) {
                    throw new IllegalMoveException("The fields are not adjacent to each other");
                } else {
                    Boolean stone = grid.getStone(posX, posY);
                    checkTurns(stone);
                    grid.moveStoneToAdjacentField(posX, posY, toPosX, toPosY);
                }
            }
        } else {
            Boolean stone = grid.getStone(posX, posY);
            checkTurns(stone);
            grid.jumpStone(posX, posY, toPosX, toPosY);
        }
    }
}
