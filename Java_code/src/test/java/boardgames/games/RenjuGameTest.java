package boardgames.games;

import boardgames.core.Coordinate;
import boardgames.core.GameResult;
import boardgames.core.Move;
import boardgames.core.PlayerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RenjuGameTest {
    private RenjuGame game;

    @BeforeEach
    void setUp() {
        game = new RenjuGame();
    }

    @Test
    @DisplayName("Початковий стан: дошка 15х15, хід першого гравця, гра триває")
    void testInitialState() {
        assertEquals("Рендзю (Gomoku)", game.getName());
        assertEquals(PlayerRole.FIRST, game.getCurrTurn());
        assertFalse(game.getResult().isFinished());
        assertEquals(15 * 15, game.getLegalMoves().size());
    }

    @Test
    @DisplayName("makeMove встановлює камінь і перемикає чергу")
    void testMakeMoveValid() {
        Coordinate c = new Coordinate(7, 7);
        assertTrue(game.makeMove(new Move.Place(c)));
        assertEquals(PlayerRole.SECOND, game.getCurrTurn());
        assertEquals(1, game.getHistory().size());
    }

    @Test
    @DisplayName("makeMove забороняє ставити в уже зайняту клітинку")
    void testMakeMoveOccupied() {
        Coordinate c = new Coordinate(7, 7);
        assertTrue(game.makeMove(new Move.Place(c)));
        assertFalse(game.makeMove(new Move.Place(c)));
        assertEquals(PlayerRole.SECOND, game.getCurrTurn());
    }

    @Test
    @DisplayName("Горизонтальна перемога: 5 каменів поспіль приносять перемогу")
    void testHorizontalWin() {
        // Рядок 7: FIRST ставить (7, 0..4), SECOND ставить у рядок 8 (8, 0..3)
        for (int col = 0; col < 4; col++) {
            assertTrue(game.makeMove(new Move.Place(new Coordinate(7, col))));
            assertTrue(game.makeMove(new Move.Place(new Coordinate(8, col))));
        }
        // 5-й вирішальний камінь першого гравця
        assertTrue(game.makeMove(new Move.Place(new Coordinate(7, 4))));

        assertTrue(game.getResult().isFinished());
        assertTrue(game.getResult().isWin());
        assertEquals(PlayerRole.FIRST, game.getResult().winner().orElseThrow());
    }

    @Test
    @DisplayName("Діагональна перемога першого гравця")
    void testDiagonalWin() {
        for (int i = 0; i < 4; i++) {
            game.makeMove(new Move.Place(new Coordinate(i, i)));       // FIRST (0,0), (1,1)...
            game.makeMove(new Move.Place(new Coordinate(i, i + 1)));   // SECOND
        }
        game.makeMove(new Move.Place(new Coordinate(4, 4)));           // FIRST 5-й у діагональ

        assertTrue(game.getResult().isFinished());
        assertEquals(PlayerRole.FIRST, game.getResult().winner().orElseThrow());
    }
}
