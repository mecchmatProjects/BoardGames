package boardgames.games.tictactoe;

import boardgames.core.Board;
import boardgames.core.Coordinate;
import boardgames.core.FixedBoard;
import boardgames.core.Game;
import boardgames.core.GameResult;
import boardgames.core.Move;
import boardgames.core.PlayerRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Хрестики-нулики на квадратному полі із налаштовуваним розміром та довжиною виграшної лінії.
 */
public class TicTacToeGame implements Game<Move.Place> {

    private static final int MIN_SIZE = 3;

    /** Напрямки (рядок, стовпець): горизонталь, вертикаль, дві діагоналі. */
    private static final int[][] DIRECTIONS = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

    private final int size;
    private final int winLength;
    private final Board<PlayerRole> board;

    private PlayerRole currentTurn = PlayerRole.FIRST;
    private GameResult result = GameResult.inProgress();
    private int movesCount = 0;

    /** Класична гра 3x3. */
    public TicTacToeGame() {
        this(GameMode.CLASSIC);
    }

    public TicTacToeGame(GameMode mode) {
        this(mode.getSize(), mode.getWinLength());
    }

    public TicTacToeGame(int size, int winLength) {
        if (size < MIN_SIZE) {
            throw new IllegalArgumentException("Розмір поля має бути не менше " + MIN_SIZE);
        }
        if (winLength < MIN_SIZE || winLength > size) {
            throw new IllegalArgumentException(
                    "Довжина лінії має бути від " + MIN_SIZE + " до " + size);
        }
        this.size = size;
        this.winLength = winLength;
        this.board = new FixedBoard<>(size, size);
    }

    @Override
    public String getName() {
        return "Хрестики-нулики";
    }

    @Override
    public String getRules() {
        return "Гравці по черзі ставлять свої знаки на поле " + size + "x" + size + ". "
                + "Перемагає той, хто першим вибудує лінію з " + winLength + " своїх знаків "
                + "по горизонталі, вертикалі або діагоналі. "
                + "Якщо поле заповнене і лінії немає — нічия.";
    }

    @Override
    public PlayerRole getCurrTurn() {
        return currentTurn;
    }

    @Override
    public GameResult getResult() {
        return result;
    }

    @Override
    public boolean makeMove(Move.Place move) {
        if (move == null || result.isFinished() || !board.isEmpty(move.getTo())) {
            return false;
        }
        board.set(move.getTo(), currentTurn);
        movesCount++;
        result = evaluateResult(move.getTo());
        if (!result.isFinished()) {
            currentTurn = currentTurn.opponent();
        }
        return true;
    }

    @Override
    public List<Move.Place> getLegalMoves() {
        List<Move.Place> moves = new ArrayList<>();
        if (result.isFinished()) {
            return moves;
        }
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board.isEmpty(new Coordinate(row, col))) {
                    moves.add(new Move.Place(row, col));
                }
            }
        }
        return moves;
    }

    /** Переможець отримує 1 бал, решта ситуацій — 0. */
    @Override
    public int getScore(PlayerRole player) {
        return result.winner().filter(player::equals).isPresent() ? 1 : 0;
    }

    public int getSize() {
        return size;
    }

    /** Read-only доступ до стану клітинки для відображення. */
    public Optional<PlayerRole> getCellOwner(Coordinate coord) {
        return board.get(coord);
    }

    private GameResult evaluateResult(Coordinate lastMove) {
        if (isWinningMove(lastMove, currentTurn)) {
            return GameResult.win(currentTurn);
        }
        if (movesCount == size * size) {
            return GameResult.draw();
        }
        return GameResult.inProgress();
    }

    private boolean isWinningMove(Coordinate lastMove, PlayerRole player) {
        for (int[] direction : DIRECTIONS) {
            int lineLength = 1
                    + countInDirection(lastMove, player, direction[0], direction[1])
                    + countInDirection(lastMove, player, -direction[0], -direction[1]);
            if (lineLength >= winLength) {
                return true;
            }
        }
        return false;
    }

    private int countInDirection(Coordinate start, PlayerRole player, int deltaRow, int deltaCol) {
        int count = 0;
        Coordinate current = start.add(deltaRow, deltaCol);
        // FixedBoard.get() поза межами повертає empty, тож цикл завершиться сам
        while (board.get(current).filter(player::equals).isPresent()) {
            count++;
            current = current.add(deltaRow, deltaCol);
        }
        return count;
    }
}