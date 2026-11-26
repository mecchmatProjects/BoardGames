package tictactoe;

import boardgames.games.tictactoe.TicTacToeConsoleSession;
import boardgames.games.tictactoe.TicTacToeGame;
import boardgames.games.tictactoe.GameMode;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicTacToeConsoleSessionTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

    private boolean run(TicTacToeGame game, String input) {
        return new TicTacToeConsoleSession(game, new Scanner(input), out).run();
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void firstPlayerWinsAndSessionReportsFinished() {
        boolean finished = run(new TicTacToeGame(), "1 1\n2 1\n1 2\n2 2\n1 3\n");
        assertTrue(finished);
        assertTrue(output().contains("Перемога: Гравець 1 (X)!"));
    }

    @Test
    void drawIsReported() {
        // ті самі ходи, що й у тесті нічиєї для гри (0-based -> 1-based)
        String moves = "1 1\n1 2\n1 3\n2 2\n2 1\n2 3\n3 2\n3 1\n3 3\n";
        assertTrue(run(new TicTacToeGame(), moves));
        assertTrue(output().contains("Нічия!"));
    }

    @Test
    void quitInterruptsGame() {
        assertFalse(run(new TicTacToeGame(), "1 1\nq\n"));
        assertTrue(output().contains("Гру перервано."));
    }

    @Test
    void endOfInputInterruptsGame() {
        assertFalse(run(new TicTacToeGame(), "1 1\n"));
        assertTrue(output().contains("Гру перервано."));
    }

    @Test
    void invalidInputShowsErrorAndAsksAgain() {
        assertFalse(run(new TicTacToeGame(), "a b\n9 9\nq\n"));
        String text = output();
        assertTrue(text.contains("не ціле число"));
        assertTrue(text.contains("в межах від 1 до 3"));
    }

    @Test
    void occupiedCellShowsErrorAndKeepsTurn() {
        assertFalse(run(new TicTacToeGame(), "1 1\n1 1\nq\n"));
        assertTrue(output().contains("вже зайнята"));
    }

    @Test
    void infiniteModeAcceptsNegativeCoordinates() {
        TicTacToeGame game = new TicTacToeGame(GameMode.INFINITE);
        String moves = "-1 -1\n5 5\n-1 0\n5 6\n-1 1\n5 7\n-1 2\n5 8\n-1 3\n";
        assertTrue(run(game, moves));
        assertTrue(output().contains("Перемога: Гравець 1 (X)!"));
    }

    @Test
    void infiniteModeRejectsHugeCoordinates() {
        assertFalse(run(new TicTacToeGame(GameMode.INFINITE), "100000 100000\nq\n"));
        assertTrue(output().contains("в межах від -50 до 50"));
    }
}