package boardgames.games.tictactoe;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        Optional<GameMode> mode = new GameModeSelector(scanner, out).select();
        if (mode.isEmpty()) {
            return;
        }
        new TicTacToeConsoleSession(new TicTacToeGame(mode.get()), scanner, out).run();
    }
}