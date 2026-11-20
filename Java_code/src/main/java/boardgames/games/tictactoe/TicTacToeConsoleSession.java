package boardgames.games.tictactoe;

import boardgames.core.GameResult;
import boardgames.core.Move;
import boardgames.core.PlayerRole;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Консольний ігровий цикл для двох гравців.
 */
public class TicTacToeConsoleSession {

    private static final String QUIT_COMMAND = "q";

    private final TicTacToeGame game;
    private final TicTacToeRenderer renderer;
    private final MoveInputParser parser;
    private final Scanner scanner;
    private final PrintStream out;

    public TicTacToeConsoleSession(TicTacToeGame game, Scanner scanner, PrintStream out) {
        this.game = game;
        this.renderer = new TicTacToeRenderer();
        this.parser = new MoveInputParser(game.getSize());
        this.scanner = scanner;
        this.out = out;
    }

    public void run() {
        printWelcome();
        while (!game.getResult().isFinished()) {
            out.println();
            out.println(renderer.render(game));
            if (!playTurn()) {
                out.println("Гру перервано.");
                return;
            }
        }
        out.println();
        out.println(renderer.render(game));
        printResult(game.getResult());
    }

    private void printWelcome() {
        out.println("=== " + game.getName() + " ===");
        out.println(game.getRules());
        out.println("Вводьте хід як \"рядок стовпець\" (наприклад: "
                + (game.isInfinite() ? "2 -3" : "2 3")
                + "). Для виходу — \"" + QUIT_COMMAND + "\".");
    }

    /**
     * Запитує хід, доки не буде введено коректний.
     * @return {@code false}, якщо користувач вирішив вийти або ввід закінчився
     */
    private boolean playTurn() {
        PlayerRole player = game.getCurrTurn();
        while (true) {
            out.printf("%s (%c), ваш хід: ", player.getDefaultName(), TicTacToeRenderer.symbolOf(player));
            if (!scanner.hasNextLine()) {
                return false;
            }
            String line = scanner.nextLine();
            if (QUIT_COMMAND.equalsIgnoreCase(line.trim())) {
                return false;
            }
            if (tryMakeMove(line)) {
                return true;
            }
        }
    }

    private boolean tryMakeMove(String line) {
        try {
            Move.Place move = parser.parse(line);
            if (game.makeMove(move)) {
                return true;
            }
            out.println("Помилка: ця клітинка вже зайнята.");
        } catch (IllegalArgumentException e) {
            out.println("Помилка: " + e.getMessage());
        }
        return false;
    }

    private void printResult(GameResult result) {
        result.winner().ifPresentOrElse(
                winner -> out.printf("Перемога: %s (%c)!%n",
                        winner.getDefaultName(), TicTacToeRenderer.symbolOf(winner)),
                () -> out.println("Нічия!"));
    }
}