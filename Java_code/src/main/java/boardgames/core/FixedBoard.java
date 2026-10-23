package boardgames.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FixedBoard<T> implements Board<T> {
    private final int rows;
    private final int cols;
    private final List<List<T>> grid;

    public FixedBoard(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Розміри дошки мають бути більшими за 0");
        }
        this.rows = rows;
        this.cols = cols;
        this.grid = new ArrayList<>(rows);
        for (int r = 0; r < rows; r++) {
            List<T> row = new ArrayList<>(Collections.nCopies(cols, null));
            this.grid.add(row);
        }
    }

    @Override
    public Optional<T> get(Coordinate coord) {
        if (!contains(coord)) {
            return Optional.empty();
        }
        return Optional.ofNullable(grid.get(coord.row()).get(coord.col()));
    }

    @Override
    public void set(Coordinate coord, T piece) {
        if (!contains(coord)) {
            throw new IllegalArgumentException("Координата виходить за межі дошки: " + coord);
        }
        grid.get(coord.row()).set(coord.col(), piece);
    }

    @Override
    public void clear(Coordinate coord) {
        if (contains(coord)) {
            grid.get(coord.row()).set(coord.col(), null);
        }
    }

    @Override
    public boolean contains(Coordinate coord) {
        return coord.row() >= 0 && coord.row() < rows &&
               coord.col() >= 0 && coord.col() < cols;
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }
}
