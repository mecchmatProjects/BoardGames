package boardgames.games.tictactoe;

import boardgames.core.Board;
import boardgames.core.Coordinate;
import boardgames.core.FixedBoard;
import boardgames.core.Game;
import boardgames.core.GameResult;
import boardgames.core.InfiniteBoard;
import boardgames.core.Move;
import boardgames.core.PlayerRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Хрестики-нулики з налаштовуваним розміром та довжиною виграшної лінії.
 * Розмір 0 означає нескінченне поле ({@link InfiniteBoard}), інакше
 * використовується квадратне {@link FixedBoard}.
 */
public class TicTacToeGame implements Game<Move.Place> {

    private static final int MIN_SIZE = 3;
    private static final int INFINITE = 0;

    /** Напрямки (рядок, стовпець): горизонталь, вертикаль, дві діагоналі. */
    private static final int[][] DIRECTIONS = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

    private final int size; // 0 = нескінченне поле
    private final int winLength;
    private final Board<PlayerRole> board;

    /** Зайняті клітинки (потрібні для відображення та ходів на нескінченному полі). */
    private final Set<Coordinate> occupied = new LinkedHashSet<>();

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
        boolean infinite = size == INFINITE;
        if (!infinite && size < MIN_SIZE) {
            throw new IllegalArgumentException("Розмір поля має бути не менше " + MIN_SIZE);
        }
        if (winLength < MIN_SIZE || (!infinite && winLength > size)) {
            throw new IllegalArgumentException("Некоректна довжина лінії: " + winLength);
        }
        this.size = size;
        this.winLength = winLength;
        this.board = infinite ? new InfiniteBoard<>() : new FixedBoard<>(size, size);
    }

    @Override
    public String getName() {
        return "Хрестики-нулики";
    }

    @Override
    public String getRules() {
        String field = isInfinite() ? "нескінченне поле" : "поле " + size + "x" + size;
        return "Гравці по черзі ставлять свої знаки на " + field + ". "
                + "Перемагає той, хто першим вибудує лінію з " + winLength + " своїх знаків "
                + "по горизонталі, вертикалі або діагоналі."
                + (isInfinite()
                ? " Координати можуть бути від'ємними."
                : " Якщо поле заповнене і лінії немає — нічия.");
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
        occupied.add(move.getTo());
        movesCount++;
        result = evaluateResult(move.getTo());
        if (!result.isFinished()) {
            currentTurn = currentTurn.opponent();
        }
        return true;
    }

    @Override
    public List<Move.Place> getLegalMoves() {
        if (result.isFinished()) {
            return new ArrayList<>();
        }
        if (isInfinite()) {
            return infiniteLegalMoves();
        }
        List<Move.Place> moves = new ArrayList<>();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board.isEmpty(new Coordinate(row, col))) {
                    moves.add(new Move.Place(row, col));
                }
            }
        }
        return moves;
    }

    /** Переможець отримує 1 бал; програш і нічия — 0. */
    @Override
    public int getScore(PlayerRole player) {
        return result.winner().filter(player::equals).isPresent() ? 1 : 0;
    }

    /** Розмір поля (0 для нескінченного). */
    public int getSize() {
        return size;
    }

    public boolean isInfinite() {
        return size == INFINITE;
    }

    /** Read-only доступ до стану клітинки для відображення. */
    public Optional<PlayerRole> getCellOwner(Coordinate coord) {
        return board.get(coord);
    }

    /** Read-only набір зайнятих клітинок. */
    public Set<Coordinate> getOccupiedCoordinates() {
        return Collections.unmodifiableSet(occupied);
    }

    /** Для нескінченного поля: клітинки поруч із зайнятими (або центр, якщо поле порожнє). */
    private List<Move.Place> infiniteLegalMoves() {
        if (occupied.isEmpty()) {
            return new ArrayList<>(List.of(new Move.Place(0, 0)));
        }
        Set<Coordinate> candidates = new LinkedHashSet<>();
        for (Coordinate c : occupied) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    Coordinate neighbor = c.add(dr, dc);
                    if (board.isEmpty(neighbor)) {
                        candidates.add(neighbor);
                    }
                }
            }
        }
        List<Move.Place> moves = new ArrayList<>();
        for (Coordinate c : candidates) {
            moves.add(new Move.Place(c));
        }
        return moves;
    }

    private GameResult evaluateResult(Coordinate lastMove) {
        if (isWinningMove(lastMove, currentTurn)) {
            return GameResult.win(currentTurn);
        }
        if (!isInfinite() && movesCount == size * size) {
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
        // FixedBoard.get() поза межами повертає empty, InfiniteBoard — порожню клітинку,
        // тож цикл завершиться сам
        while (board.get(current).filter(player::equals).isPresent()) {
            count++;
            current = current.add(deltaRow, deltaCol);
        }
        return count;
    }
}