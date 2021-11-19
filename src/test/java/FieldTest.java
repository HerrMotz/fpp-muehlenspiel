import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FieldTest {
    Field field;
    @BeforeEach
    void setup() {
        field = new Field();
    }

    @Test
    void getStone() {
        try {
            Boolean stone = field.getStone(0, 0);
        } catch (IllegalMoveException e) {
            fail("No exception expected, but got IllegalMoveException");
        }

        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            field.getStone(3, 3);
        }, "IllegalMoveException was expected");
        Assertions.assertEquals("The given x- or y-positions do not exist in a nine men's morris game. There is no field at this position.", thrown.getMessage());
    }

    @Test
    void placeStoneOnStone() {
        try {
            field.placeStone(0, 0, Field.COLOUR_BLACK);
        } catch (IllegalMoveException e) {
            System.out.println(e.getMessage());
        }

        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            field.placeStone(0, 0, Field.COLOUR_BLACK);
        }, "IllegalMoveException was expected");

        Assertions.assertEquals("There already is a stone at this position.", thrown.getMessage());
        System.out.println(thrown.getMessage());
    }

    @Test
    void placeStoneOutsideTheField() {
        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            field.placeStone(-1, 8, Field.COLOUR_BLACK);
        }, "IllegalMoveException was expected");

        Assertions.assertEquals("The given x- or y-positions do not exist in a nine men's morris game. " +
                "A field is 7x7 and the given values are out of bounds.", thrown.getMessage());
        System.out.println(thrown.getMessage());
    }

    @Test
    void placeStoneInsideTheFieldButAtAnInvalidOne() {
        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            field.placeStone(0, 1, Field.COLOUR_BLACK);
        }, "IllegalMoveException was expected");

        Assertions.assertEquals("The given x- or y-positions do not exist in a nine men's morris game. There is no field at this position.", thrown.getMessage());
        System.out.println(thrown.getMessage());
    }

    @Test
    void removeAStoneWhereThereIsNone() {
        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            field.removeStone(0, 0);
        }, "IllegalMoveException was expected");

        Assertions.assertEquals("There is no stone at the given field.", thrown.getMessage());
        System.out.println(thrown.getMessage());
    }

    @Test
    void removeAStone() {
        try {
            field.placeStone(3, 4, Field.COLOUR_WHITE);
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        try {
            field.removeStone(3,4);
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }
    }

    @Test
    void moveStone() {
        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            field.moveStone(1, 3, 1, 5);
        }, "IllegalMoveException was expected");

        Assertions.assertEquals("You may only move stones, so please choose a not empty field.", thrown.getMessage());
        System.out.println(thrown.getMessage());

        try {
            field.placeStone(1, 3, Field.COLOUR_WHITE);
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        try {
            field.moveStone(1, 3, 1, 5);
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        try {
            field.placeStone(1, 3, Field.COLOUR_WHITE);
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            field.moveStone(1, 5, 1, 3);
        }, "IllegalMoveException was expected");

        Assertions.assertEquals("You may only move stones to empty fields.", thrown.getMessage());
        System.out.println(thrown.getMessage());

        thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            field.moveStone(1, 5, 1, 5);
        }, "IllegalMoveException was expected");

        Assertions.assertEquals("A move to the same field is not allowed.", thrown.getMessage());
        System.out.println(thrown.getMessage());
    }

    @Test
    void getField() {
        Boolean[][] result = field.getField();
        for (int i = 0; i < Field.LIMIT_X; i++) {
            for (int j = 0; j < Field.LIMIT_Y; j++) {
                assertNull(result[i][j]);
            }
        }

        try {
            field.placeStone(0, 0, Field.COLOUR_WHITE);
            field.placeStone(1, 1, Field.COLOUR_BLACK);
            field.placeStone(1, 5, Field.COLOUR_BLACK);
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        result = field.getField();
        assertEquals(result[0][0], Field.COLOUR_WHITE);
        assertEquals(result[1][1], Field.COLOUR_BLACK);
        assertEquals(result[5][1], Field.COLOUR_BLACK);

        Arrays.stream(result).forEach((i) -> {
            Arrays.stream(i).forEach((j) ->  {
                String m = "o";
                if (j != null)
                    m = j ? "W" : "B";
                System.out.print(m + " ");
            });
            System.out.println();
        });
    }
}