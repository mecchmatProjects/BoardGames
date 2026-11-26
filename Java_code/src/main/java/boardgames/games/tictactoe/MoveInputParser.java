package boardgames.games.tictactoe;

import boardgames.core.Move;

/**
 * Перетворює введення користувача на хід {@link Move.Place}.
 * <p>
 * Для скінченного поля вводиться "рядок стовпець" з нумерацією з 1
 * (всередині перетворюється на 0-based). Для нескінченного поля
 * координати беруться як є, можуть бути від'ємними, але за модулем
 * не більшими за {@link #INFINITE_LIMIT}.
 */
public class MoveInputParser {

    /** Максимальний модуль координати на нескінченному полі (захист від надвеликого виводу та переповнення). */
    public static final int INFINITE_LIMIT = 50;

    private final boolean infinite;
    private final int boardSize;

    public MoveInputParser(TicTacToeGame game) {
        this.infinite = game.isInfinite();
        this.boardSize = game.getSize();
    }

    /**
     * @throws IllegalArgumentException з повідомленням, придатним для показу користувачеві
     */
    public Move.Place parse(String input) {
        String example = infinite ? "2 -3" : "2 3";
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
        if (infinite) {
            validateInfiniteRange(row);
            validateInfiniteRange(col);
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

    private void validateInfiniteRange(int value) {
        if (Math.abs((long) value) > INFINITE_LIMIT) {
            throw new IllegalArgumentException(
                    "Координати мають бути в межах від -" + INFINITE_LIMIT + " до " + INFINITE_LIMIT + ".");
        }
    }
}