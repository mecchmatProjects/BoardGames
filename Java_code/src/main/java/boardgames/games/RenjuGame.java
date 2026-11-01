package boardgames.games;

import boardgames.core.Coordinate;
import boardgames.core.FixedBoard;
import boardgames.core.Game;
import boardgames.core.GameResult;
import boardgames.core.Move;
import boardgames.core.PlayerRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Реалізація гри Рендзю (Гомоку / 5 в ряд) на стандартній дошці 15x15.
 */
public class RenjuGame implements Game<Move.Place> {
    public static final int DEFAULT_SIZE = 15;
    private static final int WIN_STREAK = 5;

    private final FixedBoard<PlayerRole> board;
    private final int boardSize;
    private final List<Move.Place> moveHistory;
    private PlayerRole currTurn;
    private GameResult result;

    public RenjuGame() {
        this(DEFAULT_SIZE);
    }

    public RenjuGame(int boardSize) {
        if (boardSize < WIN_STREAK) {
            throw new IllegalArgumentException("Розмір дошки має бути не менше " + WIN_STREAK);
        }
        this.boardSize = boardSize;
        this.board = new FixedBoard<>(boardSize, boardSize);
        this.moveHistory = new ArrayList<>();
        this.currTurn = PlayerRole.FIRST;
        this.result = GameResult.inProgress();
    }

    @Override
    public String getName() {
        return "Рендзю";
    }

    @Override
    public String getRules() {
        return "Гравці по черзі ставлять фішки на перетини сітки " + boardSize + "x" + boardSize + ". " +
               "Перемагає той, хто першим збудує безперервну лінію з 5 своїх фішок " +
               "по горизонталі, вертикалі або діагоналі.";
    }

    @Override
    public PlayerRole getCurrTurn() {
        return currTurn;
    }

    @Override
    public GameResult getResult() {
        return result;
    }

    @Override
    public boolean makeMove(Move.Place move) {
        if (result.isFinished() || move == null) {
            return false;
        }

        Coordinate coord = move.getTo();
        if (!board.contains(coord) || !board.isEmpty(coord)) {
            return false;
        }

        board.set(coord, currTurn);
        moveHistory.add(move);

        if (checkWin(coord, currTurn)) {
            result = GameResult.win(currTurn);
            return true;
        }

        if (moveHistory.size() == boardSize * boardSize) {
            result = GameResult.draw();
            return true;
        }

        currTurn = currTurn.opponent();
        return true;
    }

    @Override
    public List<Move.Place> getLegalMoves() {
        if (result.isFinished()) {
            return Collections.emptyList();
        }

        List<Move.Place> legalMoves = new ArrayList<>();
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                Coordinate coord = new Coordinate(r, c);
                if (board.isEmpty(coord)) {
                    legalMoves.add(new Move.Place(coord));
                }
            }
        }
        return legalMoves;
    }

    @Override
    public int getScore(PlayerRole player) {
        if (!result.isFinished()) {
            return 0;
        }
        return result.winner()
                .filter(w -> w == player)
                .map(w -> 1)
                .orElse(0);
    }

    public FixedBoard<PlayerRole> getBoard() {
        return board;
    }

    public List<Move.Place> getHistory() {
        return Collections.unmodifiableList(moveHistory);
    }

    public int getBoardSize() {
        return boardSize;
    }

    private boolean checkWin(Coordinate lastMove, PlayerRole player) {
        int[][] directions = {
                {0, 1},   // горизонталь
                {1, 0},   // вертикаль
                {1, 1},   // головна діагональ
                {1, -1}   // побічна діагональ
        };

        for (int[] dir : directions) {
            int dRow = dir[0];
            int dCol = dir[1];

            int count = 1;
            count += countInDirection(lastMove, dRow, dCol, player);
            count += countInDirection(lastMove, -dRow, -dCol, player);

            if (count >= WIN_STREAK) {
                return true;
            }
        }
        return false;
    }

    private int countInDirection(Coordinate start, int dRow, int dCol, PlayerRole player) {
        int matches = 0;
        int row = start.row() + dRow;
        int col = start.col() + dCol;

        while (row >= 0 && row < boardSize && col >= 0 && col < boardSize) {
            Coordinate nextCoord = new Coordinate(row, col);
            if (board.get(nextCoord).filter(p -> p == player).isPresent()) {
                matches++;
                row += dRow;
                col += dCol;
            } else {
                break;
            }
        }
        return matches;
    }
}
