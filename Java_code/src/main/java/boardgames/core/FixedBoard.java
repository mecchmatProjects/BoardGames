package boardgames.core;
import java.util.Optional;


public class FixedBoard<T> implements Board<T> {
    private final int rows;
    private final int cols;
    private final Object[][] grid;

    public FixedBoard(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Розміри дошки мають бути більшими за 0");
        }
        this.rows = rows;
        this.cols = cols;
        this.grid = new Object[rows][cols];
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<T> get(Coordinate coord) {
        if (!contains(coord)) {
            return Optional.empty();
        }
        return Optional.ofNullable((T) grid[coord.row()][coord.col()]);
    }

    @Override
    public void set(Coordinate coord, T piece) {
        if (!contains(coord)) {
            throw new IllegalArgumentException("Координата виходить за межі дошки: " + coord);
        }
        grid[coord.row()][coord.col()] = piece;
    }

    @Override
    public void clear(Coordinate coord) {
        if (contains(coord)){
            grid[coord.row()][coord.col()] = null;
        }
    }

    @Override
    public boolean contains(Coordinate coord) {
        return coord.row() >= 0 && coord.row() < rows &&
                coord.col() >= 0 && coord.col() < cols;
    }


    public int rows() { return rows; }
    public int cols() { return cols; }
}
