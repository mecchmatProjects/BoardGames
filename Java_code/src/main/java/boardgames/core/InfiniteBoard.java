package boardgames.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InfiniteBoard<T> implements Board<T> {
    private final Map<Coordinate, T> cells = new HashMap<>();


    @Override
    public Optional<T> get(Coordinate coord) {
        return Optional.ofNullable(cells.get(coord));
    }

    @Override
    public void set(Coordinate coord, T piece) {
        cells.put(coord, piece);
    }

    @Override
    public void clear(Coordinate coord) {
        cells.remove(coord);
    }

    @Override
    public boolean contains(Coordinate coord) {
        return true; //нескінченне поле не має ліміту, тому все може їй належати
    }

    @Override
    public boolean isEmpty(Coordinate coord) {
        return !cells.containsKey(coord);
    }

    public Map<Coordinate, T> getOccupiedCells() {
        return Collections.unmodifiableMap(cells);
    }
}
