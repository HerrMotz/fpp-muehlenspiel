import backend.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("CodeBlock2Expr")
class GridTest {
    Grid grid;
    @BeforeEach
    void setup() {
        grid = new Grid();
    }

    @Test
    void getStone() {
        try {
            grid.getField(0, 0);
        } catch (IllegalMoveException e) {
            fail("No exception expected, but got backend.IllegalMoveException");
        }

        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            grid.getField(3, 3);
        }, "backend.IllegalMoveException was expected");
        Assertions.assertEquals("The given x- or y-positions do not exist in a nine men's morris game. There is no field at this position.", thrown.getMessage());
    }

    @Test
    void placeStoneOnStone() {
        try {
            grid.placeStone(0, 0, new Stone(Grid.COLOUR_BLACK));
        } catch (IllegalMoveException e) {
            System.out.println(e.getMessage());
        }

        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            grid.placeStone(0, 0, new Stone(Grid.COLOUR_BLACK));
        }, "backend.IllegalMoveException was expected");

        Assertions.assertEquals("There already is a stone at this position.", thrown.getMessage());
        System.out.println(thrown.getMessage());
    }

    @Test
    void placeStoneOutsideTheField() {
        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            grid.placeStone(-1, 8, new Stone(Grid.COLOUR_BLACK));
        }, "backend.IllegalMoveException was expected");

        Assertions.assertEquals("The given x- or y-positions do not exist in a nine men's morris game. " +
                "A grid is 7x7 and the given values are out of bounds.", thrown.getMessage());
        System.out.println(thrown.getMessage());
    }

    @Test
    void placeStoneInsideTheFieldButAtAnInvalidOne() {
        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            grid.placeStone(0, 1, new Stone(Grid.COLOUR_BLACK));
        }, "backend.IllegalMoveException was expected");

        Assertions.assertEquals("The given x- or y-positions do not exist in a nine men's morris game. There is no field at this position.", thrown.getMessage());
        System.out.println(thrown.getMessage());
    }

    @Test
    void removeAStoneWhereThereIsNone() {
        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            grid.removeStone(0, 0);
        }, "backend.IllegalMoveException was expected");

        Assertions.assertEquals("There is no stone at the given field.", thrown.getMessage());
        System.out.println(thrown.getMessage());
    }

    @Test
    void removeAStone() {
        try {
            grid.placeStone(3, 4, new Stone(Grid.COLOUR_WHITE));
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        try {
            grid.removeStone(3,4);
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }
    }

    @Test
    void jumpStone() {
        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            grid.jumpStone(1, 3, 1, 5);
        }, "backend.IllegalMoveException was expected");

        Assertions.assertEquals("You may only move stones, so please choose a not empty field.", thrown.getMessage());
        System.out.println(thrown.getMessage());

        try {
            grid.placeStone(1, 3, new Stone(Grid.COLOUR_WHITE));
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        try {
            grid.jumpStone(1, 3, 1, 5);
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        try {
            grid.placeStone(1, 3, new Stone(Grid.COLOUR_WHITE));
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            grid.jumpStone(1, 5, 1, 3);
        }, "backend.IllegalMoveException was expected");

        Assertions.assertEquals("You may only move stones to empty fields.", thrown.getMessage());
        System.out.println(thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            grid.jumpStone(1, 5, 1, 5);
        }, "backend.IllegalMoveException was expected");

        Assertions.assertEquals("A move to the same field is not allowed.", thrown.getMessage());
        System.out.println(thrown.getMessage());
    }

    @Test
    void getField() {
        Field[][] result = grid.getGrid();
        for (int i = 0; i < Grid.LIMIT_X; i++) {
            for (int j = 0; j < Grid.LIMIT_Y; j++) {
                assertTrue(result[i][j].isEmpty());
            }
        }

        try {
            grid.placeStone(0, 0, new Stone(Grid.COLOUR_WHITE));
            grid.placeStone(1, 1, new Stone(Grid.COLOUR_BLACK));
            grid.placeStone(1, 5, new Stone(Grid.COLOUR_BLACK));
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        result = grid.getGrid();
        assertEquals(result[0][0].getStone().getColour(), Grid.COLOUR_WHITE);
        assertEquals(result[1][1].getStone().getColour(), Grid.COLOUR_BLACK);
        assertEquals(result[5][1].getStone().getColour(), Grid.COLOUR_BLACK);

        System.out.println(grid);
    }

    @Test
    void validityCheckOfFields() {
        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            grid.placeStone(4, 5, new Stone(Grid.COLOUR_WHITE));
        }, "backend.IllegalMoveException was expected");

        Assertions.assertEquals("The given x- or y-positions do not exist in a nine men's morris game. There is no field at this position.", thrown.getMessage());
        System.out.println(thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            grid.placeStone(6, 5, new Stone(Grid.COLOUR_WHITE));
        }, "backend.IllegalMoveException was expected");

        Assertions.assertEquals("The given x- or y-positions do not exist in a nine men's morris game. There is no field at this position.", thrown.getMessage());
        System.out.println(thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            grid.getField(5, 2);
        }, "backend.IllegalMoveException was expected");

        Assertions.assertEquals("The given x- or y-positions do not exist in a nine men's morris game. There is no field at this position.", thrown.getMessage());
        System.out.println(thrown.getMessage());

        try {
            grid.placeStone(5, 3, new Stone(Grid.COLOUR_WHITE));
            grid.placeStone(4, 4, new Stone(Grid.COLOUR_BLACK));
            grid.placeStone(6, 3, new Stone(Grid.COLOUR_BLACK));
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }
    }

    @Test
    void getAdjacentFields() {
        Set<Field> adjacentFields;

        try {
            grid.placeStone(1, 3, new Stone(Grid.COLOUR_WHITE));
            grid.placeStone(1, 5, new Stone(Grid.COLOUR_BLACK));
            grid.placeStone(2, 3, new Stone(Grid.COLOUR_WHITE));
            grid.placeStone(0, 3, new Stone(Grid.COLOUR_BLACK));
            grid.placeStone(1, 1, new Stone(Grid.COLOUR_WHITE));

            adjacentFields = grid.getAdjacentFields(1, 3);
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
            return;
        }

        try {
            assertTrue(adjacentFields.contains(grid.getField(2, 3)));
            assertTrue(adjacentFields.contains(grid.getField(1, 5)));
            assertTrue(adjacentFields.contains(grid.getField(0, 3)));
            assertTrue(adjacentFields.contains(grid.getField(1, 1)));

            adjacentFields = grid.getAdjacentFields(1, 1);
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
            return;
        }

        try {
            assertTrue(adjacentFields.contains(grid.getField(1, 3)));
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }
    }

    @Test
    void areFieldsAdjacent() {
        try {
            assertTrue(grid.areFieldsAdjacent(0,0, 0, 3));
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        try {
            assertFalse(grid.areFieldsAdjacent(3, 1, 2, 2));
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        try {
            assertFalse(grid.areFieldsAdjacent(1, 1, 1, 1));
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        try {
            assertFalse(grid.areFieldsAdjacent(1, 1, 2, 2));
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        // Test if the center is avoided
        try {
            assertFalse(grid.areFieldsAdjacent(4,3, 2,3));
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
           assertFalse(grid.areFieldsAdjacent(3,1, 0,2));
        }, "backend.IllegalMoveException was expected");

        Assertions.assertEquals("The given x- or y-positions do not exist in a nine men's morris game. There is no field at this position.", thrown.getMessage());
        System.out.println(thrown.getMessage());
    }

    @Test
    void moveStoneToAdjacentField() {
        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            grid.moveStoneToAdjacentField(5,3,4,2);
        }, "backend.IllegalMoveException was expected");

        Assertions.assertEquals("The fields are not adjacent to each other.", thrown.getMessage());
        System.out.println(thrown.getMessage());

        try {
            grid.placeStone(2,3, new Stone(Grid.COLOUR_WHITE));
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        try {
            grid.moveStoneToAdjacentField(2, 3, 2, 2);
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        try {
            grid.moveStoneToAdjacentField(2, 2, 3, 2);
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }
    }

    @Test
    void getTwoConsecutiveFields() {
        try {
            grid.generateMills();
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }
        System.out.println(grid.getPossibleMills().size());
        System.out.println(grid.getPossibleMills());
    }
}