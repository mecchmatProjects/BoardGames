package boardgames.games;

import boardgames.core.Coordinate;
import boardgames.core.FixedBoard;
import boardgames.core.Game;
import boardgames.core.GameResult;
import boardgames.core.Move;
import boardgames.core.PlayerRole;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Реалізація гри "Точки" (захоплення вузлів у полон шляхом оточення).
 */
public class DotsGame implements Game<Move.Place> {
    public static final int DEFAULT_SIZE = 15;

    private final FixedBoard<PlayerRole> board;
    private final int boardSize;
    private final List<Move.Place> moveHistory;
    private PlayerRole currTurn;
    private GameResult result;

    private int firstPlayerCaptures;
    private int secondPlayerCaptures;

    public DotsGame() {
        this(DEFAULT_SIZE);
    }

    public DotsGame(int boardSize) {
        if (boardSize < 3) {
            throw new IllegalArgumentException("Розмір дошки для точок має бути не менше 3");
        }
        this.boardSize = boardSize;
        this.board = new FixedBoard<>(boardSize, boardSize);
        this.moveHistory = new ArrayList<>();
        this.currTurn = PlayerRole.FIRST;
        this.result = GameResult.inProgress();
        this.firstPlayerCaptures = 0;
        this.secondPlayerCaptures = 0;
    }

    @Override
    public String getName() {
        return "Точки";
    }

    @Override
    public String getRules() {
        return "Гравці по черзі ставлять точки свого кольору у вузли сітки " + boardSize + "x" + boardSize + ". " +
               "Якщо гравець оточує безперервним контуром точки суперника, вони беруться в полон " +
               "і фарбуються в колір загарбника. Перемагає той, хто захопив більше точок.";
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

        // Перевірка захоплення ворожих точок після поточного ходу
        processCaptures(currTurn);

        // Перевірка завершення ходів (дошка заповнена)
        if (moveHistory.size() == boardSize * boardSize) {
            finishGame();
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

        List<Move.Place> legal = new ArrayList<>();
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                Coordinate coord = new Coordinate(r, c);
                if (board.isEmpty(coord)) {
                    legal.add(new Move.Place(coord));
                }
            }
        }
        return legal;
    }

    @Override
    public int getScore(PlayerRole player) {
        return player == PlayerRole.FIRST ? firstPlayerCaptures : secondPlayerCaptures;
    }

    public void finishGame() {
        if (firstPlayerCaptures > secondPlayerCaptures) {
            result = GameResult.win(PlayerRole.FIRST);
        } else if (secondPlayerCaptures > firstPlayerCaptures) {
            result = GameResult.win(PlayerRole.SECOND);
        } else {
            result = GameResult.draw();
        }
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

    /**
     * Алгоритм пошуку оточених областей через обхід у ширину (BFS).
     * Знаходить усі зв'язні області чужих точок або порожнеч, які не мають виходу до краю дошки.
     */
    private void processCaptures(PlayerRole capturer) {
        PlayerRole victim = capturer.opponent();
        boolean[][] visited = new boolean[boardSize][boardSize];

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                Coordinate coord = new Coordinate(r, c);
                if (!visited[r][c] && board.get(coord).filter(p -> p == victim).isPresent()) {
                    List<Coordinate> component = new ArrayList<>();
                    boolean reachesBoundary = floodFillCheck(coord, capturer, visited, component);

                    // Якщо замкнена область суперника не має виходу до краю — точки полонені
                    if (!reachesBoundary && !component.isEmpty()) {
                        for (Coordinate captured : component) {
                            board.set(captured, capturer);
                            if (capturer == PlayerRole.FIRST) {
                                firstPlayerCaptures++;
                            } else {
                                secondPlayerCaptures++;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean floodFillCheck(Coordinate start, PlayerRole boundaryColor, boolean[][] visited, List<Coordinate> component) {
        Queue<Coordinate> queue = new ArrayDeque<>();
        queue.add(start);
        visited[start.row()][start.col()] = true;

        boolean reachesEdge = false;
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        while (!queue.isEmpty()) {
            Coordinate curr = queue.poll();
            component.add(curr);

            if (curr.row() == 0 || curr.row() == boardSize - 1 || curr.col() == 0 || curr.col() == boardSize - 1) {
                reachesEdge = true;
            }

            for (int[] d : dirs) {
                int nr = curr.row() + d[0];
                int nc = curr.col() + d[1];

                if (nr >= 0 && nr < boardSize && nc >= 0 && nc < boardSize) {
                    Coordinate next = new Coordinate(nr, nc);
                    boolean isBarrier = board.get(next).filter(p -> p == boundaryColor).isPresent();
                    if (!isBarrier && !visited[nr][nc]) {
                        visited[nr][nc] = true;
                        queue.add(next);
                    }
                }
            }
        }
        return reachesEdge;
    }
}
