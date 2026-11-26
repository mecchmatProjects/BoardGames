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
        GameModeSelector selector = new GameModeSelector(scanner, out);

        while (true) {
            Optional<GameMode> mode = selector.select();
            if (mode.isEmpty()) {
                return;
            }
            boolean finished = new TicTacToeConsoleSession(new TicTacToeGame(mode.get()), scanner, out).run();
            if (!finished || !askPlayAgain(scanner, out)) {
                return;
            }
            out.println();
        }
    }

    private static boolean askPlayAgain(Scanner scanner, PrintStream out) {
        while (true) {
            out.print("Зіграти ще раз? (y/n): ");
            if (!scanner.hasNextLine()) {
                return false;
            }
            String answer = scanner.nextLine().trim().toLowerCase();
            switch (answer) {
                case "y", "yes", "так", "т" -> {
                    return true;
                }
                case "n", "no", "ні", "н", ConsoleCommands.QUIT -> {
                    return false;
                }
                default -> out.println("Помилка: введіть \"y\" або \"n\".");
            }
        }
    }
}