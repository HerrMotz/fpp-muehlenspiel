public class Field {
    public static final boolean COLOUR_BLACK = false;
    public static final boolean COLOUR_WHITE = true;
    public static final Boolean NO_STONE = null;

    public static int LIMIT_X = 7;
    public static int LIMIT_Y = 7;

    private Boolean[][] field = new Boolean[LIMIT_X][LIMIT_Y];

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
    private void checkValidityOfFieldPosition(int posX, int posY) throws IllegalArgumentException, ArrayIndexOutOfBoundsException, IllegalMoveException {
        if (posX >= LIMIT_X || posY >= LIMIT_Y || posX < 0 || posY < 0) {
            throw new IllegalMoveException("The given x- or y-positions do not exist in a nine men's morris game. A field is 7x7 and the given values are out of bounds.");
        }
        if (    (posX == 0 && (posY == 1 || posY == 2 || posY == 4 || posY == 5)) ||
                (posX == 1 && (posY == 0 || posY == 2 || posY == 4 || posY == 6)) ||
                (posX == 2 && (posY == 0 || posY == 1 || posY == 5 || posY == 6)) ||
                (posX == 3 && posY == 3)
        ) {
            throw new IllegalMoveException("The given x- or y-positions do not exist in a nine men's morris game. There is no field at this position.");
        }
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

    /**
     * Gives the whole field as an array.
     * @return the game's field
     */
    @SuppressWarnings("unused")
    public Boolean[][] getField() {
        return field;
    }
}
