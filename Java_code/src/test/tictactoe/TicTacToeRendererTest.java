package tictactoe;

import boardgames.core.Move;
import boardgames.core.PlayerRole;
import boardgames.games.tictactoe.GameMode;
import boardgames.games.tictactoe.TicTacToeGame;
import boardgames.games.tictactoe.TicTacToeRenderer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicTacToeRendererTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final TicTacToeRenderer renderer =
            new TicTacToeRenderer(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    private static String lines(String... rows) {
        return String.join(System.lineSeparator(), rows);
    }

    @Test
    void rendersEmptyBoard() {
        String expected = lines(
                "   1   2   3",
                "1    |   |   ",
                "  ---+---+---",
                "2    |   |   ",
                "  ---+---+---",
                "3    |   |   ");
        assertEquals(expected, renderer.format(new TicTacToeGame()));
    }

    @Test
    void rendersPlacedMarks() {
        TicTacToeGame game = new TicTacToeGame();
        game.makeMove(new Move.Place(0, 0)); // X
        game.makeMove(new Move.Place(0, 1)); // O
        game.makeMove(new Move.Place(1, 1)); // X
        game.makeMove(new Move.Place(2, 0)); // O

        String expected = lines(
                "   1   2   3",
                "1  X | O |   ",
                "  ---+---+---",
                "2    | X |   ",
                "  ---+---+---",
                "3  O |   |   ");
        assertEquals(expected, renderer.format(game));
    }

    @Test
    void mapsPlayersToSymbols() {
        assertEquals('X', TicTacToeRenderer.symbolOf(PlayerRole.FIRST));
        assertEquals('O', TicTacToeRenderer.symbolOf(PlayerRole.SECOND));
    }

    @Test
    void renderWritesBoardToOutputStream() {
        TicTacToeGame game = new TicTacToeGame();
        renderer.render(game);
        String expected = renderer.format(game) + System.lineSeparator();
        assertEquals(expected, buffer.toString(StandardCharsets.UTF_8));
    }

    @Test
    void renderRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> renderer.render(null));
    }

    // ---------- нескінченне поле (GameMode.INFINITE) ----------

    @Test
    void rendersEmptyInfiniteBoardAroundOrigin() {
        String expected = lines(
                "      -2 -1  0  1  2",
                "  -2   .  .  .  .  .",
                "  -1   .  .  .  .  .",
                "   0   .  .  .  .  .",
                "   1   .  .  .  .  .",
                "   2   .  .  .  .  .");
        assertEquals(expected, renderer.format(new TicTacToeGame(GameMode.INFINITE)));
    }

    @Test
    void rendersMarkOnInfiniteBoard() {
        TicTacToeGame game = new TicTacToeGame(GameMode.INFINITE);
        game.makeMove(new Move.Place(0, 0)); // X
        String expected = lines(
                "      -2 -1  0  1  2",
                "  -2   .  .  .  .  .",
                "  -1   .  .  .  .  .",
                "   0   .  .  X  .  .",
                "   1   .  .  .  .  .",
                "   2   .  .  .  .  .");
        assertEquals(expected, renderer.format(game));
    }

    @Test
    void infiniteBoardGrowsToIncludeFarAndNegativeCells() {
        TicTacToeGame game = new TicTacToeGame(GameMode.INFINITE);
        game.makeMove(new Move.Place(0, 0));   // X
        game.makeMove(new Move.Place(3, -2));  // O

        String[] rendered = renderer.format(game).split(System.lineSeparator());
        // рядки -2..5 (8 шт.) + заголовок
        assertEquals(9, rendered.length);
        // стовпці -4..2 (7 шт.) -> заголовок
        assertEquals("      -4 -3 -2 -1  0  1  2", rendered[0]);
        assertEquals("   0   .  .  .  .  X  .  .", rendered[3]); // рядок 0
        assertEquals("   3   .  .  O  .  .  .  .", rendered[6]); // рядок 3, стовпець -2
    }

    @Test
    void infiniteRenderWritesToOutputStream() {
        TicTacToeGame game = new TicTacToeGame(GameMode.INFINITE);
        renderer.render(game);
        assertEquals(renderer.format(game) + System.lineSeparator(), buffer.toString(StandardCharsets.UTF_8));
    }
}