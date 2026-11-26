package boardgames.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CoordinateTest {
    @Test
    @DisplayName("Перевірка коректного збереження row та col")
    void testCoordinateCreation() {
        Coordinate coord = new Coordinate(3, 5);
        assertEquals(3, coord.row());
        assertEquals(5, coord.col());
    }

    @Test
    @DisplayName("Метод add має повертати нову координату зі зміщеними значеннями")
    void testAdd() {
        Coordinate start = new Coordinate(2, 3);
        Coordinate shifted = start.add(1, -2);

        assertEquals(3, shifted.row());
        assertEquals(1, shifted.col());
    }

    @Test
    @DisplayName("Два різні об'єкти Coordinate з однаковими координатами рівні через equals")
    void testEqualsAndHashCode() {
        Coordinate c1 = new Coordinate(4, 7);
        Coordinate c2 = new Coordinate(4, 7);
        Coordinate c3 = new Coordinate(4, 8);

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, c3);
    }
}
