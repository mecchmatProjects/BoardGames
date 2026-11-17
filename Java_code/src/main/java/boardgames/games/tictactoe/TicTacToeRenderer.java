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
        String separator = "  " + String.join("+", Collections.nCopies(size, "---"));

        List<String> lines = new ArrayList<>();
        lines.add(renderHeader(size));
        for (int row = 0; row < size; row++) {
            if (row > 0) {
                lines.add(separator);
            }
            lines.add(renderRow(game, row));
        }
        return String.join(System.lineSeparator(), lines);
    }

    private String renderHeader(int size) {
        List<String> labels = new ArrayList<>();
        for (int col = 1; col <= size; col++) {
            labels.add(String.valueOf(col));
        }
        return "   " + String.join("   ", labels);
    }

    private String renderRow(TicTacToeGame game, int row) {
        List<String> cells = new ArrayList<>();
        for (int col = 0; col < game.getSize(); col++) {
            cells.add(String.valueOf(cellSymbol(game, new Coordinate(row, col))));
        }
        return (row + 1) + " " + " " + String.join(" | ", cells) + " ";
    }

    private char cellSymbol(TicTacToeGame game, Coordinate coord) {
        return game.getCellOwner(coord).map(TicTacToeRenderer::symbolOf).orElse(EMPTY_CELL);
    }
}