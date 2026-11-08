package boardgames.games;

import boardgames.core.Coordinate;
import boardgames.core.Game;
import boardgames.core.Move;
import boardgames.core.PlayerRole;
import boardgames.session.BoardRenderer;

public class RenjuConsoleRenderer implements BoardRenderer<Move.Place> {
    @Override
    public void render(Game<Move.Place> game) {
        if (!(game instanceof RenjuGame renju)) {
            return;
        }

        var board = renju.getBoard();
        int size = renju.getBoardSize();

        System.out.println();
        System.out.print("    ");
        for (int c = 0; c < size; c++) {
            System.out.printf("%2d ", c);
        }
        System.out.println();

        for (int r = 0; r < size; r++) {
            System.out.printf("%2d |", r);
            for (int c = 0; c < size; c++) {
                var piece = board.get(new Coordinate(r, c));
                String symbol = " . ";
                if (piece.isPresent()) {
                    symbol = piece.get() == PlayerRole.FIRST ? " X " : " O ";
                }
                System.out.print(symbol);
            }
            System.out.printf("| %d%n", r);
        }

        System.out.print("    ");
        for (int c = 0; c < size; c++) {
            System.out.printf("%2d ", c);
        }
        System.out.println("\n");
    }
}
