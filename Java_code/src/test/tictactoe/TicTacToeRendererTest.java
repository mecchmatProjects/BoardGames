package tictactoe;

import boardgames.core.Move;
import boardgames.core.PlayerRole;
import boardgames.games.tictactoe.TicTacToeGame;
import boardgames.games.tictactoe.TicTacToeRenderer;

public class TicTacToeRendererTest {

    private static final TicTacToeRenderer RENDERER = new TicTacToeRenderer();
    private static int failed = 0;

    public static void main(String[] args) {
        rendersEmptyBoard();
        rendersPlacedMarks();
        mapsPlayersToSymbols();

        System.out.println();
        System.out.println(failed == 0 ? "Усі тести пройдено." : "Провалено тестів: " + failed);
    }

    private static void rendersEmptyBoard() {
        String expected = lines(
                "   1   2   3",
                "1    |   |   ",
                "  ---+---+---",
                "2    |   |   ",
                "  ---+---+---",
                "3    |   |   ");
        check("rendersEmptyBoard", expected, RENDERER.render(new TicTacToeGame()));
    }

    private static void rendersPlacedMarks() {
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
        check("rendersPlacedMarks", expected, RENDERER.render(game));
    }

    private static void mapsPlayersToSymbols() {
        check("symbol FIRST", 'X', TicTacToeRenderer.symbolOf(PlayerRole.FIRST));
        check("symbol SECOND", 'O', TicTacToeRenderer.symbolOf(PlayerRole.SECOND));
    }

    private static String lines(String... rows) {
        return String.join(System.lineSeparator(), rows);
    }

    private static void check(String name, Object expected, Object actual) {
        if (expected.equals(actual)) {
            System.out.println("OK   " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
            System.out.println("--- очікувалось:");
            System.out.println(expected);
            System.out.println("--- отримано:");
            System.out.println(actual);
        }
    }
}