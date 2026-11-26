package tictactoe;

import boardgames.games.tictactoe.GameMode;
import boardgames.games.tictactoe.GameModeSelector;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameModeSelectorTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

    private Optional<GameMode> select(String input) {
        return new GameModeSelector(new Scanner(input), out).select();
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void selectsModeByNumber() {
        assertEquals(Optional.of(GameMode.CLASSIC), select("1\n"));
        assertEquals(Optional.of(GameMode.MEDIUM), select("2\n"));
        assertEquals(Optional.of(GameMode.LARGE), select("3\n"));
        assertEquals(Optional.of(GameMode.INFINITE), select("4\n"));
    }

    @Test
    void trimsWhitespaceAroundChoice() {
        assertEquals(Optional.of(GameMode.MEDIUM), select("  2  \n"));
    }

    @Test
    void quitReturnsEmptyCaseInsensitive() {
        assertTrue(select("q\n").isEmpty());
        assertTrue(select("Q\n").isEmpty());
    }

    @Test
    void endOfInputReturnsEmpty() {
        assertTrue(select("").isEmpty());
    }

    @Test
    void invalidChoicesShowErrorAndAskAgain() {
        assertEquals(Optional.of(GameMode.LARGE), select("abc\n0\n5\n3\n"));
        assertTrue(output().contains("Помилка"));
    }

    @Test
    void printsMenuWithAllModes() {
        select("q\n");
        String text = output();
        for (GameMode mode : GameMode.values()) {
            assertTrue(text.contains(mode.getLabel()), "У меню немає режиму " + mode);
        }
    }
}