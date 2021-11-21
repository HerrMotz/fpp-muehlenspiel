import java.util.HashSet;
import java.util.Set;

public class Grid {
    public static final boolean COLOUR_BLACK = false;
    public static final boolean COLOUR_WHITE = true;
    public static final Boolean NO_STONE = null;

    public final int LIMIT_X;
    public final int LIMIT_Y;

    @SuppressWarnings("FieldMayBeFinal")
    private Boolean[][] grid;

    public Grid(int limitX, int limitY) {
        LIMIT_X = limitX;
        LIMIT_Y = limitY;
        grid = new Boolean[LIMIT_X][LIMIT_Y];
    }

    /**
     * Checks, whether a grid position is valid or out of bounds (helper function)
     * @throws ArrayIndexOutOfBoundsException When the posX or posY are out of bounds (7x7 grid)
     * @throws IllegalArgumentException When the given position is illegal for the game's grid (e.g. x=0, y=1)
     * @param posX The field's x-position in the grid
     * @param posY The field's y-position in the grid
     *
     * @throws IllegalArgumentException If the position is invalid on the grid
     * @throws ArrayIndexOutOfBoundsException If the position is out of bounds for the grid
     */
    private void checkValidityOfFieldPosition(int posX, int posY) throws IllegalMoveException {
        if (posX >= LIMIT_X || posY >= LIMIT_Y || posX < 0 || posY < 0) {
            throw new IllegalMoveException("The given x- or y-positions do not exist in a nine men's morris game. A grid is 7x7 and the given values are out of bounds.");
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

    /**
     * Helper Function for getting all adjacent fields
     * @param moveCoordinate The coordinate which we move along to find adjacent fields
     * @param limitMoveCoordinate The limit, where the grid ends
     * @param step Whether we move in the negative or positive direction
     * @param fixCoordinate The fixed coordinate (the line on which we search for adjacent fields)
     * @param xComesFirst Sets the order in which we store the coordinates in the result array
     * @return A set of all adjacent fields
     */
    private Set<Pair<Integer, Integer>> getAdjacentFieldsLoop(int moveCoordinate, int limitMoveCoordinate, int step, int fixCoordinate, boolean xComesFirst) {
        Set<Pair<Integer, Integer>> adjacentFields = new HashSet<>();

        while (moveCoordinate < limitMoveCoordinate && moveCoordinate >= 0) {
            moveCoordinate = moveCoordinate + step;
            try {
                checkValidityOfFieldPosition(moveCoordinate, fixCoordinate);
                if (xComesFirst) adjacentFields.add(new Pair<>(moveCoordinate, fixCoordinate));
                else adjacentFields.add(new Pair<>(fixCoordinate, moveCoordinate));
                break;
            } catch (IllegalMoveException ignored) {}
        }

        return adjacentFields;
    }

    /**
     * Gets all adjacent fields to a given field
     * @param posX x-position of the field
     * @param posY y-position of the field
     * @return a set of adjacent fields
     * @throws IllegalMoveException should the given coordinates of the field be invalid
     */
    public Set<Pair<Integer, Integer>> getAdjacentFields(int posX, int posY) throws IllegalMoveException {
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
        Set<Pair<Integer, Integer>> adjacentFields = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            adjacentFields.addAll(switch (i) {
                case 0 -> getAdjacentFieldsLoop(posX, LIMIT_X,  1, posY, true);
                case 1 -> getAdjacentFieldsLoop(posX, LIMIT_X, -1, posY, true);
                case 2 -> getAdjacentFieldsLoop(posY, LIMIT_Y,  1, posX, false);
                case 3 -> getAdjacentFieldsLoop(posY, LIMIT_Y, -1, posX, false);
                default -> throw new IllegalStateException("Unexpected value: " + i);
            });
        }

        return adjacentFields;
}

    /**
     * Check whether two given fields are adjacent to each other
     * @param posX x-position of first field
     * @param posY y-position of first field
     * @param posX2 x-position of second field
     * @param posY2 y-position of second field
     * @return true if they are adjacent, false if nt
     * @throws IllegalMoveException should either one of the fields not be valid
     */
    public boolean areFieldsAdjacent(int posX, int posY, int posX2, int posY2) throws IllegalMoveException {
        checkValidityOfFieldPosition(posX, posY);
        checkValidityOfFieldPosition(posX2, posY2);

        Set<Pair<Integer, Integer>> adjFields = getAdjacentFields(posX, posY);
        @SuppressWarnings("SuspiciousNameCombination") Pair<Integer, Integer> pos2 = new Pair<>(posX2, posY2);

        return adjFields.contains(pos2);
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
        return grid[posY][posX];
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
        grid[posY][posX] = NO_STONE;
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
        grid[posY][posX] = colour;
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
    public void jumpStone(int posX, int posY, int toPosX, int toPosY) throws IllegalMoveException {
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
        if (areFieldsAdjacent(posX, posY, toPosX, toPosY)) {
            jumpStone(posX, posY, toPosX, toPosY);
        } else {
            throw new IllegalMoveException("The fields are not adjacent to each other.");
        }
    }

    /**
     * Gives the whole grid as an array.
     * @return the game's grid
     */
    @SuppressWarnings("unused")
    public Boolean[][] getGrid() {
        return grid;
    }
}
