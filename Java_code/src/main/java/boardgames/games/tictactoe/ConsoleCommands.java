package boardgames.games.tictactoe;

/**
 * Спільні команди консольного вводу.
 */
final class ConsoleCommands {

    static final String QUIT = "q";

    private ConsoleCommands() {
    }

    static boolean isQuit(String line) {
        return line != null && QUIT.equalsIgnoreCase(line.trim());
    }
}