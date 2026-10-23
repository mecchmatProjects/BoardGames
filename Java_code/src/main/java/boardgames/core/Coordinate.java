package boardgames.core;

/**
 * Незмінна координата клітинки на дошці.
 */
public record Coordinate(int row, int col) {
    public Coordinate add(int deltaRow, int deltaCol){
        return new Coordinate(row+deltaRow, col+deltaCol);
    }

    @Override
    public String toString(){
        return String.format("(%d, %d)", row, col);
    }
}
