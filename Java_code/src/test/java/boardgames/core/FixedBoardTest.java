package boardgames.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FixedBoardTest {
    private FixedBoard<String> board;

    @BeforeEach
    void setUp() {
        board = new FixedBoard<>(8, 8);
    }

    @Test
    @DisplayName("Конструктор викидає виняток при некоректних розмірах")
    void testInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new FixedBoard<>(0, 8));
        assertThrows(IllegalArgumentException.class, () -> new FixedBoard<>(8, -1));
    }

    @Test
    @DisplayName("contains правильно перевіряє межі дошки")
    void testContains() {
        assertTrue(board.contains(new Coordinate(0, 0)));
        assertTrue(board.contains(new Coordinate(7, 7)));
        assertFalse(board.contains(new Coordinate(-1, 0)));
        assertFalse(board.contains(new Coordinate(0, 8)));
    }

    @Test
    @DisplayName("set та get коректно зберігають та повертають значення")
    void testSetAndGet() {
        Coordinate coord = new Coordinate(3, 4);
        assertTrue(board.isEmpty(coord));

        board.set(coord, "PIECE");
        assertFalse(board.isEmpty(coord));
        assertTrue(board.get(coord).isPresent());
        assertEquals("PIECE", board.get(coord).get());
    }

    @Test
    @DisplayName("clear очищає клітинку")
    void testClear() {
        Coordinate coord = new Coordinate(2, 2);
        board.set(coord, "TEST");
        assertFalse(board.isEmpty(coord));

        board.clear(coord);
        assertTrue(board.isEmpty(coord));
        assertTrue(board.get(coord).isEmpty());
    }

    @Test
    @DisplayName("set за межі дошки викликає IllegalArgumentException")
    void testSetOutOfBounds() {
        Coordinate out = new Coordinate(10, 10);
        assertThrows(IllegalArgumentException.class, () -> board.set(out, "FAIL"));
    }
}
