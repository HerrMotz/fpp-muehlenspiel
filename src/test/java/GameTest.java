import backend.Game;
import backend.Grid;
import backend.IllegalMoveException;
import backend.Stone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("CodeBlock2Expr")
public class GameTest {
    Game game;
    @BeforeEach
    void setup() {
        game = new Game();
    }

    @Test
    void firstMove() {
        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            game.placeStone(6, 6, new Stone(Grid.COLOUR_BLACK));
        }, "backend.IllegalMoveException was expected");
        Assertions.assertEquals("It's the other player's turn.", thrown.getMessage());

        try {
            game.placeStone(6, 6, new Stone(Grid.COLOUR_WHITE));
        } catch (IllegalMoveException e) {
            fail("No exception expected, but got backend.IllegalMoveException");
        }

        thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            game.placeStone(0, 0, new Stone(Grid.COLOUR_WHITE));
        }, "backend.IllegalMoveException was expected");
        Assertions.assertEquals("It's the other player's turn.", thrown.getMessage());
    }

    void makeSomeMoves1() {
        try {
            game.placeStone(0, 0, new Stone(Grid.COLOUR_WHITE));
            game.placeStone(1, 1, new Stone(Grid.COLOUR_BLACK));
            game.placeStone(2, 2, new Stone(Grid.COLOUR_WHITE));
            game.placeStone(0, 3, new Stone(Grid.COLOUR_BLACK));
            game.placeStone(1, 3, new Stone(Grid.COLOUR_WHITE));
            game.placeStone(2, 3, new Stone(Grid.COLOUR_BLACK));
            game.placeStone(4, 3, new Stone(Grid.COLOUR_WHITE));
        } catch (IllegalMoveException e) {
            fail("No exception expected, but got backend.IllegalMoveException");
        }
    }

    void makeSomeMoves2() {
        try {
            game.placeStone(0, 6, new Stone(Grid.COLOUR_BLACK));
            game.placeStone(1, 5, new Stone(Grid.COLOUR_WHITE));
            game.placeStone(3, 6, new Stone(Grid.COLOUR_BLACK));
            game.placeStone(3, 4, new Stone(Grid.COLOUR_WHITE));
            game.placeStone(5, 3, new Stone(Grid.COLOUR_BLACK));
        } catch (IllegalMoveException e) {
            fail("No exception expected, but got backend.IllegalMoveException");
        }
    }

    void makeSomeMoves3() {
        try {
            game.placeStone(3, 0, new Stone(Grid.COLOUR_WHITE));
            game.placeStone(6, 0, new Stone(Grid.COLOUR_BLACK));
            game.placeStone(5, 1, new Stone(Grid.COLOUR_WHITE));
            game.placeStone(4, 2, new Stone(Grid.COLOUR_BLACK));
            game.placeStone(4, 4, new Stone(Grid.COLOUR_WHITE));
            game.placeStone(6, 6, new Stone(Grid.COLOUR_BLACK));
        } catch (IllegalMoveException e) {
            fail("No exception expected, but got backend.IllegalMoveException");
        }
    }

    @Test
    void stoneCounter() {
        makeSomeMoves1();

        assertEquals(5, game.getStonesInInventory(Grid.COLOUR_WHITE));

        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            game.placeStone(6, 6, new Stone(Grid.COLOUR_WHITE));
        }, "backend.IllegalMoveException was expected");
        Assertions.assertEquals("It's the other player's turn.", thrown.getMessage());

        makeSomeMoves2();

        System.out.println(game);

        assertEquals(3, game.getStonesInInventory(Grid.COLOUR_WHITE));
        assertEquals(3, game.getStonesInInventory(Grid.COLOUR_BLACK));

        makeSomeMoves3();

        System.out.println(game);

        assertEquals(0, game.getStonesInInventory(Grid.COLOUR_WHITE));
        assertEquals(0, game.getStonesInInventory(Grid.COLOUR_BLACK));

        thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            game.placeStone(3, 1, new Stone(Grid.COLOUR_BLACK));
        }, "backend.IllegalMoveException was expected");
        Assertions.assertEquals("The game is currently not in the place phase.", thrown.getMessage());
    }

    @Test
    void moveStone() {
        makeSomeMoves1();
        makeSomeMoves2();
        makeSomeMoves3();

        System.out.println(game);

        try {
            game.moveStone(3, 4, 2, 4);
        } catch (IllegalMoveException e) {
            fail("No exception expected, but got backend.IllegalMoveException");
        }

        IllegalMoveException thrown = Assertions.assertThrows(IllegalMoveException.class, () -> {
            game.moveStone(3, 6, 3, 4);
        }, "backend.IllegalMoveException was expected");
        Assertions.assertEquals("The fields are not adjacent to each other.", thrown.getMessage());

        try {
            game.moveStone(3, 6, 3, 5);
        } catch (IllegalMoveException e) {
            fail("No exception expected, but got backend.IllegalMoveException");
        }

        System.out.println(game);
    }

    @Test
    void checkMill() {
        try {
            game.placeStone(0,0, new Stone(Grid.COLOUR_WHITE));
            game.placeStone(1,1, new Stone(Grid.COLOUR_BLACK));
            game.placeStone(3,0, new Stone(Grid.COLOUR_WHITE));
            game.placeStone(0,6, new Stone(Grid.COLOUR_BLACK));
            game.placeStone(6,0, new Stone(Grid.COLOUR_WHITE));
        } catch (IllegalMoveException e) {
            fail("Expected no exception, got: " + e.getMessage());
        }

        assertTrue(game.checkMill(0,0));
        assertTrue(game.checkMill(3, 0));
        assertTrue(game.checkMill(6, 0));
        assertFalse(game.checkMill(0, 3));
    }
}
