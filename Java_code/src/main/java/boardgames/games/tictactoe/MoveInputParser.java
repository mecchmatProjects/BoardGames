package boardgames.games.tictactoe;

import boardgames.core.Move;

/**
 * Перетворює введення користувача на хід {@link Move.Place}.
 * <p>
 * Для скінченного поля вводиться "рядок стовпець" з нумерацією з 1
 * (всередині перетворюється на 0-based). Для нескінченного поля
 * (boardSize == 0) координати беруться як є і можуть бути від'ємними.
 */
public class MoveInputParser {

    private static final int INFINITE = 0;

    private final int boardSize;

    public MoveInputParser(int boardSize) {
        this.boardSize = boardSize;
    }

    /**
     * @throws IllegalArgumentException з повідомленням, придатним для показу користувачеві
     */
    public Move.Place parse(String input) {
        String example = boardSize == INFINITE ? "2 -3" : "2 3";
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Порожній ввід. Приклад: " + example);
        }
        String[] tokens = input.trim().split("[\\s,]+");
        if (tokens.length != 2) {
            throw new IllegalArgumentException(
                    "Потрібно два числа: рядок і стовпець. Приклад: " + example);
        }
        int row = parseNumber(tokens[0]);
        int col = parseNumber(tokens[1]);
        if (boardSize == INFINITE) {
            return new Move.Place(row, col);
        }
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