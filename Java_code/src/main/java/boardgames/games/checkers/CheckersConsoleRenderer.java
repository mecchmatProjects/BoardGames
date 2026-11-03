package boardgames.games.checkers;

import boardgames.core.Coordinate;
import boardgames.core.Game;
import boardgames.session.BoardRenderer;

public class CheckersConsoleRenderer implements BoardRenderer<CheckersMove>  {
    @Override
    public void render(Game<CheckersMove> game) {
        AbstractCheckersGame checkers = (AbstractCheckersGame) game;
        var board = checkers.getBoard();
        int size = board.rows();

        System.out.println();
        System.out.print("    ");
        for (int c = 0; c < size; c++) {
            System.out.print(c + "   ");
        }
        System.out.println();

        System.out.print("  +");
        for (int c = 0; c < size; c++) {
            System.out.print("---+");
        }
        System.out.println();

        for (int r = 0; r < size; r++) {
            System.out.printf("%d |", r);
            for (int c = 0; c < size; c++) {
                var pieceOpt = board.get(new Coordinate(r, c));
                String cellSymbol;
                if (pieceOpt.isPresent()) {
                    cellSymbol = pieceOpt.get().toString();
                } else {
                    cellSymbol = (r + c) % 2 != 0 ? "·" : " ";
                }
                System.out.printf(" %s |", cellSymbol);
            }
            System.out.printf(" %d%n", r);

            System.out.print("  +");
            for (int c = 0; c < size; c++) {
                System.out.print("---+");
            }
            System.out.println();
        }

        System.out.print("    ");
        for (int c = 0; c < size; c++) {
            System.out.print(c + "   ");
        }
        System.out.println("\n");
    }
}
