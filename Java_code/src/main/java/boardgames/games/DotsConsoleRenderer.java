package boardgames.games;

import boardgames.core.Coordinate;
import boardgames.core.Game;
import boardgames.core.Move;
import boardgames.core.PlayerRole;
import boardgames.session.BoardRenderer;

public class DotsConsoleRenderer implements BoardRenderer<Move.Place> {
    @Override
    public void render(Game<Move.Place> game) {
        if (!(game instanceof DotsGame dots)) {
            return;
        }

        var board = dots.getBoard();
        int size = dots.getBoardSize();

        System.out.println();
        System.out.printf("Рахунок: Червоні (FIRST): %d | Сині (SECOND): %d%n",
                dots.getScore(PlayerRole.FIRST), dots.getScore(PlayerRole.SECOND));

        System.out.print("    ");
        for (int c = 0; c < size; c++) {
            System.out.printf("%2d ", c);
        }
        System.out.println();

        for (int r = 0; r < size; r++) {
            System.out.printf("%2d |", r);
            for (int c = 0; c < size; c++) {
                var point = board.get(new Coordinate(r, c));
                String symbol = " + ";
                if (point.isPresent()) {
                    symbol = point.get() == PlayerRole.FIRST ? " R " : " B ";
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
