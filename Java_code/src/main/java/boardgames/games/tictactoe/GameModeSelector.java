package boardgames.games.tictactoe;


import boardgames.games.tictactoe.GameMode;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;

/**
 * Консольне меню вибору режиму гри.
 */
public class GameModeSelector {

    private static final String QUIT_COMMAND = "q";

    private final Scanner scanner;
    private final PrintStream out;

    public GameModeSelector(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
    }

    /**
     * @return обраний режим, або {@link Optional#empty()}, якщо користувач вийшов
     *         або ввід закінчився
     */
    public Optional<GameMode> select() {
        GameMode[] modes = GameMode.values();
        printMenu(modes);
        while (true) {
            out.print("Ваш вибір: ");
            if (!scanner.hasNextLine()) {
                return Optional.empty();
            }
            String line = scanner.nextLine().trim();
            if (QUIT_COMMAND.equalsIgnoreCase(line)) {
                return Optional.empty();
            }
            Optional<GameMode> mode = parseChoice(line, modes);
            if (mode.isPresent()) {
                return mode;
            }
            out.println("Помилка: введіть число від 1 до " + modes.length + " або \"" + QUIT_COMMAND + "\".");
        }
    }

    private void printMenu(GameMode[] modes) {
        out.println("Оберіть розмір поля:");
        for (int i = 0; i < modes.length; i++) {
            out.printf("  %d — %s%n", i + 1, modes[i].getLabel());
        }
        out.println("  " + QUIT_COMMAND + " — вихід");
    }

    private Optional<GameMode> parseChoice(String line, GameMode[] modes) {
        try {
            int index = Integer.parseInt(line) - 1;
            if (index >= 0 && index < modes.length) {
                return Optional.of(modes[index]);
            }
        } catch (NumberFormatException ignored) {
            // нечисловий ввід обробляється як невдалий вибір
        }
        return Optional.empty();
    }
}