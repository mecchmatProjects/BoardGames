package boardgames.games.tictactoe;

import boardgames.core.Coordinate;
import boardgames.core.PlayerRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Відповідає лише за текстове представлення поля та знаків гравців.
 */
public class TicTacToeRenderer {

    private static final char EMPTY_CELL = ' ';

    public static char symbolOf(PlayerRole player) {
        return switch (player) {
            case FIRST -> 'X';
            case SECOND -> 'O';
        };
    }

    public String render(TicTacToeGame game) {
        int size = game.getSize();
        int width = String.valueOf(size).length();
        String separator = " ".repeat(width + 1)
                + String.join("+", Collections.nCopies(size, "-".repeat(width + 2)));

        List<String> lines = new ArrayList<>();
        lines.add(renderHeader(size, width));
        for (int row = 0; row < size; row++) {
            if (row > 0) {
                lines.add(separator);
            }
            lines.add(renderRow(game, row, width));
        }
        return String.join(System.lineSeparator(), lines);
    }

    private String renderHeader(int size, int width) {
        List<String> labels = new ArrayList<>();
        for (int col = 1; col <= size; col++) {
            labels.add(pad(String.valueOf(col), width));
        }
        return " ".repeat(width + 2) + String.join("   ", labels);
    }

    private String renderRow(TicTacToeGame game, int row, int width) {
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