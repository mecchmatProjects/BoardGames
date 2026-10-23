package boardgames.core;
import java.util.Optional;


/**
 * Універсальний інтерфейс сітки для будь-яких типів фігур/фішок.
 * @param <T> тип фігури (наприклад, Character, String або власний enum)
 */
public interface Board<T> {

    Optional<T> get(Coordinate coord);

    void set(Coordinate coord, T piece);

    void clear(Coordinate coord);

    boolean contains(Coordinate coord);

    default boolean isEmpty(Coordinate coord){
        return  contains(coord) && get(coord).isEmpty();
    }
}
