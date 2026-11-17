package boardgames.games.tictactoe;

import boardgames.core.Move;

/**
 * Перетворює введення користувача ("рядок стовпець", нумерація з 1)
 * на хід {@link Move.Place} з 0-based координатами.
 */
public class MoveInputParser {

    private final int boardSize;

    public MoveInputParser(int boardSize) {
        this.boardSize = boardSize;
    }

    /**
     * @throws IllegalArgumentException з повідомленням, придатним для показу користувачеві
     */
    public Move.Place parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Порожній ввід. Приклад: 2 3");
        }
        String[] tokens = input.trim().split("[\\s,]+");
        if (tokens.length != 2) {
            throw new IllegalArgumentException("Потрібно два числа: рядок і стовпець. Приклад: 2 3");
        }
        int row = parseNumber(tokens[0]);
        int col = parseNumber(tokens[1]);
        validateRange(row);
        validateRange(col);
        return new Move.Place(row - 1, col - 1);
    }

    private int parseNumber(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + token + "\" — не ціле число.");
        }
    }

    private void validateRange(int value) {
        if (value < 1 || value > boardSize) {
            throw new IllegalArgumentException(
                    "Координати мають бути в межах від 1 до " + boardSize + ".");
        }
    }
}