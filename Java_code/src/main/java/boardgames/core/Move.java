package boardgames.core;

import java.util.Optional;

/**
 * Базовий інтерфейс для представлення ходу на ігровій дошці.
 */
public interface Move {

    /**
     * Цільова координата, куди здійснюється дія (постановка або переміщення).
     */
    Coordinate getTo();

    /**
     * Початкова координата (якщо це переміщення фігури).
     * Для ходів постановки повертає Optional.empty().
     */
    default Optional<Coordinate> getFrom() {
        return Optional.empty();
    }

    /**
     * Чи є хід переміщенням фігури з однієї клітинки в іншу.
     */
    default boolean isTransition() {
        return getFrom().isPresent();
    }

    /**
     * Хід-постановка (виставлення фішки в клітинку).
     * Використовується для: Хрестики-нулики, Рендзю, Го, Точки, Креноль.
     */
    record Place(Coordinate to) implements Move {
        public Place(int row, int col) {
            this(new Coordinate(row, col));
        }

        @Override
        public Coordinate getTo() {
            return to;
        }

        @Override
        public String toString() {
            return "Place" + to;
        }
    }

    /**
     * Хід-переміщення (з однієї клітинки в іншу).
     * Використовується для: Шашки, Шахи.
     */
    record Transition(Coordinate from, Coordinate to) implements Move {
        public Transition(int fromRow, int fromCol, int toRow, int toCol) {
            this(new Coordinate(fromRow, fromCol), new Coordinate(toRow, toCol));
        }

        @Override
        public Coordinate getTo() {
            return to;
        }

        @Override
        public Optional<Coordinate> getFrom() {
            return Optional.of(from);
        }

        @Override
        public String toString() {
            return from + " -> " + to;
        }
    }
}
