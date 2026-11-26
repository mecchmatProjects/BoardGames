package boardgames.games.tictactoe;

import boardgames.core.Coordinate;
import boardgames.core.Game;
import boardgames.core.Move;
import boardgames.core.PlayerRole;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Текстове представлення поля та знаків гравців.
 * {@code implements BoardRenderer<Move.Place>} та {@code @Override} до {@link #render(Game)}.
 */
public class TicTacToeRenderer {

    private static final char EMPTY_CELL = ' ';
    private static final char INFINITE_EMPTY_CELL = '.';
    private static final int INFINITE_MARGIN = 2;

    private final PrintStream out;

    public TicTacToeRenderer(PrintStream out) {
        this.out = Objects.requireNonNull(out);
    }

    public static char symbolOf(PlayerRole player) {
        return switch (player) {
            case FIRST -> 'X';
            case SECOND -> 'O';
        };
    }

    /** Виводить поле у потік, переданий у конструкторі (сигнатура відповідає очікуваному BoardRenderer). */
    public void render(Game<Move.Place> game) {
        if (!(game instanceof TicTacToeGame ticTacToe)) {
            throw new IllegalArgumentException("Очікувалась гра TicTacToeGame, отримано: "
                    + (game == null ? "null" : game.getClass().getSimpleName()));
        }
        out.println(format(ticTacToe));
    }

    /** Повертає текстове представлення поля (зручно для тестів). */
    public String format(TicTacToeGame game) {
        if (game.isInfinite()) {
            return formatInfinite(game);
        }
        int size = game.getSize();
        int width = String.valueOf(size).length();
        String separator = " ".repeat(width + 1)
                + String.join("+", Collections.nCopies(size, "-".repeat(width + 2)));

        List<String> lines = new ArrayList<>();
        lines.add(formatHeader(size, width));
        for (int row = 0; row < size; row++) {
            if (row > 0) {
                lines.add(separator);
            }
            lines.add(formatRow(game, row, width));
        }
        return String.join(System.lineSeparator(), lines);
    }

    /** Показує прямокутник навколо зайнятих клітинок (і центру) з відступом. */
    private String formatInfinite(TicTacToeGame game) {
        int minRow = 0, maxRow = 0, minCol = 0, maxCol = 0;
        for (Coordinate c : game.getOccupiedCoordinates()) {
            minRow = Math.min(minRow, c.row());
            maxRow = Math.max(maxRow, c.row());
            minCol = Math.min(minCol, c.col());
            maxCol = Math.max(maxCol, c.col());
        }
        minRow -= INFINITE_MARGIN;
        maxRow += INFINITE_MARGIN;
        minCol -= INFINITE_MARGIN;
        maxCol += INFINITE_MARGIN;

        StringBuilder sb = new StringBuilder("     ");
        for (int col = minCol; col <= maxCol; col++) {
            sb.append(String.format("%3d", col));
        }
        for (int row = minRow; row <= maxRow; row++) {
            sb.append(System.lineSeparator()).append(String.format("%4d ", row));
            for (int col = minCol; col <= maxCol; col++) {
                char symbol = game.getCellOwner(new Coordinate(row, col))
                        .map(TicTacToeRenderer::symbolOf)
                        .orElse(INFINITE_EMPTY_CELL);
                sb.append("  ").append(symbol);
            }
        }
        return sb.toString();
    }

    private String formatHeader(int size, int width) {
        List<String> labels = new ArrayList<>();
        for (int col = 1; col <= size; col++) {
            labels.add(pad(String.valueOf(col), width));
        }
        return " ".repeat(width + 2) + String.join("   ", labels);
    }

    private String formatRow(TicTacToeGame game, int row, int width) {
        List<String> cells = new ArrayList<>();
        for (int col = 0; col < game.getSize(); col++) {
            cells.add(pad(String.valueOf(cellSymbol(game, new Coordinate(row, col))), width));
        }
        return pad(String.valueOf(row + 1), width) + "  " + String.join(" | ", cells) + " ";
    }

    private char cellSymbol(TicTacToeGame game, Coordinate coord) {
        return game.getCellOwner(coord).map(TicTacToeRenderer::symbolOf).orElse(EMPTY_CELL);
    }

    private String pad(String text, int width) {
        return text + " ".repeat(width - text.length());
    }
}