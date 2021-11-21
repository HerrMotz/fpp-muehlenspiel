import java.util.ArrayList;
import java.util.List;

public class Field {
    public static final boolean COLOUR_BLACK = false;
    public static final boolean COLOUR_WHITE = true;
    public static final Boolean NO_STONE = null;

    public final int LIMIT_X;
    public final int LIMIT_Y;

    @SuppressWarnings("FieldMayBeFinal")
    private Boolean[][] field;

    public Field(int limitX, int limitY) {
        LIMIT_X = limitX;
        LIMIT_Y = limitY;
        field = new Boolean[LIMIT_X][LIMIT_Y];
    }

    /**
     * Checks, whether a field position is valid or out of bounds (helper function)
     * @throws ArrayIndexOutOfBoundsException When the posX or posY are out of bounds (7x7 grid)
     * @throws IllegalArgumentException When the given position is illegal for the game's field (e.g. x=0, y=1)
     * @param posX The field's x-position in the grid
     * @param posY The field's y-position in the grid
     *
     * @throws IllegalArgumentException If the position is invalid on the field's grid
     * @throws ArrayIndexOutOfBoundsException If the position is out of bounds for the field's grid
     */
    private void checkValidityOfFieldPosition(int posX, int posY) throws IllegalMoveException {
        if (posX >= LIMIT_X || posY >= LIMIT_Y || posX < 0 || posY < 0) {
            throw new IllegalMoveException("The given x- or y-positions do not exist in a nine men's morris game. A field is 7x7 and the given values are out of bounds.");
        }

        if (posX > Math.floorDiv(LIMIT_X, 2))
            posX = (LIMIT_X - 1 - posX) % (LIMIT_X - 1);

        if (posY > Math.floorDiv(LIMIT_Y, 2))
            posY = (LIMIT_Y - 1 - posY) % (LIMIT_Y - 1);

        if (    (posX == 0 && (posY == 1 || posY == 2)) ||
                (posX == 1 && (posY == 0 || posY == 2)) ||
                (posX == 2 && (posY == 0 || posY == 1)) ||
                (posX == 3 && posY == 3)
        ) {
            throw new IllegalMoveException("The given x- or y-positions do not exist in a nine men's morris game. There is no field at this position.");
        }
    }

    private List<Pair<Integer, Integer>> getAdjacentFieldsLoop(int moveCoordinate, int limitMoveCoordinate, int step, int fixCoordinate) {
        List<Pair<Integer, Integer>> adjacentFields = new ArrayList<>();

        while (moveCoordinate < limitMoveCoordinate && moveCoordinate >= 0) {
            moveCoordinate = moveCoordinate + step;
            try {
                checkValidityOfFieldPosition(moveCoordinate, fixCoordinate);
                adjacentFields.add(new Pair<>(moveCoordinate, fixCoordinate));
                break;
            } catch (IllegalMoveException ignored) {}
        }

        return adjacentFields;
    }

    public void getAdjacentFields(int posX, int posY) throws IllegalMoveException {
        checkValidityOfFieldPosition(posX, posY);

//        int expectedNumberOfAdjacentFields = 0;
//
//        if (posX == posY) {
//            // 2 benachbarte Felder
//            expectedNumberOfAdjacentFields = 2;
//        } else if (posX - posY == 1) {
//            // 3 benachbarte Felder
//            expectedNumberOfAdjacentFields = 3;
//        } else if (posX - posY == 2) {
//            // 4 benachbarte Felder
//            expectedNumberOfAdjacentFields = 4;
//        }
//
//        int numberOfFoundFields = 0;


        // TODO Redo this in a mathematically pleasing way
        List<Pair<Integer, Integer>> adjacentFields = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            adjacentFields.addAll(switch (i) {
                case 0 -> getAdjacentFieldsLoop(posX, LIMIT_X,  1, posY);
                case 1 -> getAdjacentFieldsLoop(posX, LIMIT_X, -1, posY);
                case 2 -> getAdjacentFieldsLoop(posY, LIMIT_Y,  1, posX);
                case 3 -> getAdjacentFieldsLoop(posY, LIMIT_Y, -1, posX);
                default -> throw new IllegalStateException("Unexpected value: " + i);
            });
        }

        System.out.println(adjacentFields);
}

    private void areFieldsAdjacent(int posX, int posY, int posX2, int posY2) throws IllegalMoveException {
        checkValidityOfFieldPosition(posX, posY);
        checkValidityOfFieldPosition(posX2, posY2);
    }

    /**
     * Determines whether there is a stone on the given field, and if so which colour it has.
     * @throws ArrayIndexOutOfBoundsException When the posX or posY are out of bounds (7x7 grid)
     * @throws IllegalArgumentException When the given position is illegal for the game's field (e.g. x=0, y=1)
     * @param posX The field's x-position in the grid.
     * @param posY The field's y-position in the grid.
     * @return null if there is no stone or black or white, if there is a stone.
     *
     * @throws IllegalArgumentException If the position is invalid on the field's grid
     * @throws ArrayIndexOutOfBoundsException If the position is out of bounds for the field's grid
     */
    public Boolean getStone(int posX, int posY) throws IllegalMoveException {
        checkValidityOfFieldPosition(posX, posY);
        return field[posY][posX];
    }

    /**
     * Takes a stone off the play-field.
     * @param posX The stone's field x-position in the grid
     * @param posY The stone's field y-position in the grid
     *
     * @throws IllegalArgumentException Should there be no stone on the given field
     * @throws IllegalArgumentException If the position is invalid on the field's grid
     * @throws ArrayIndexOutOfBoundsException If the position is out of bounds for the field's grid
     */
    public void removeStone(int posX, int posY) throws IllegalMoveException {
        Boolean stone = getStone(posX, posY);
        if (stone == NO_STONE) throw new IllegalMoveException("There is no stone at the given field.");
        field[posY][posX] = NO_STONE;
    }

    /**
     * Places a stone on the field, which may either be black or white.
     * The field is represented by a grid of 7x7 positions. Therefore, some positions are invalid, even if they are in the bounds of the array.
     * @param posX The field's x-position in the grid
     * @param posY The field's y-position in the grid
     * @param colour Either black or white
     *
     * @throws IllegalArgumentException Should there already be a stone on the given field
     * @throws IllegalArgumentException If the position is invalid on the field's grid
     * @throws ArrayIndexOutOfBoundsException If the position is out of bounds for the field's grid
     */
    public void placeStone(int posX, int posY, boolean colour) throws IllegalMoveException {
        checkValidityOfFieldPosition(posX, posY);

        if (getStone(posX, posY) != NO_STONE) {
            throw new IllegalMoveException("There already is a stone at this position.");
        }
        field[posY][posX] = colour;
    }

    /**
     * Moves a stone from one field to another.
     * @param posX The stone's field x-position in the grid
     * @param posY The stone's field y-position in the grid
     * @param toPosX The field's x-position to which the stone should be moved
     * @param toPosY The field's y-position to which the stone should be moved
     *
     * @throws IllegalArgumentException Should there be no stone on the given field
     * @throws IllegalArgumentException If the position is invalid on the field's grid
     * @throws ArrayIndexOutOfBoundsException If the position is out of bounds for the field's grid.
     */
    public void moveStone(int posX, int posY, int toPosX, int toPosY) throws IllegalMoveException {
        if (posX == toPosX && posY == toPosY)
            throw new IllegalMoveException("A move to the same field is not allowed.");

        Boolean stone = getStone(posX, posY);
        if (stone == NO_STONE)
            throw new IllegalMoveException("You may only move stones, so please choose a not empty field.");

        Boolean toStone = getStone(toPosX, toPosY);
       if (toStone != NO_STONE)
            throw new IllegalMoveException("You may only move stones to empty fields.");

        removeStone(posX, posY);
        placeStone(toPosX, toPosY, stone);
    }

    public void moveStoneToAdjacentField(int posX, int posY, int toPosX, int toPosY) throws IllegalMoveException {
        moveStone(posX, posY, toPosX, toPosY);
    }

    /**
     * Gives the whole field as an array.
     * @return the game's field
     */
    @SuppressWarnings("unused")
    public Boolean[][] getField() {
        return field;
    }
}
