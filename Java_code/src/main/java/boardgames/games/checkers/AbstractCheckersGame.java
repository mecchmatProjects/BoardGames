package boardgames.games.checkers;

import boardgames.core.*;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCheckersGame implements Game<CheckersMove> {

    protected final int size;
    protected final FixedBoard<CheckerPiece> board;
    protected PlayerRole currentTurn;
    protected GameResult result;
    protected int scoreFirst = 0;   // Скільки збив гравець 1
    protected int scoreSecond = 0;  // Скільки збив гравець 2

    public AbstractCheckersGame(int size) {
        this.size = size;
        this.board = new FixedBoard<>(size, size);
        this.currentTurn = PlayerRole.FIRST; // Перший гравець (білі) ходять знизу вгору
        this.result = GameResult.inProgress();
        setupInitialBoard();
    }

    protected abstract boolean canManCaptureBackwards(); // Чи може проста бити назад
    protected abstract boolean isFlyingKing();           // Чи є дамка "довгою" (літаючою)

    protected void setupInitialBoard() {
        int rowsOfPieces = (size == 10) ? 4 : 3;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                // Шашки ставляться тільки на темні клітинки: (row + col) % 2 != 0
                if ((r + c) % 2 != 0) {
                    if (r < rowsOfPieces) {
                        board.set(new Coordinate(r, c), new CheckerPiece(PlayerRole.SECOND, false));
                    } else if (r >= size - rowsOfPieces) {
                        board.set(new Coordinate(r, c), new CheckerPiece(PlayerRole.FIRST, false));
                    }
                }
            }
        }
    }


    @Override
    public PlayerRole getCurrTurn(){
        return currentTurn;
    }

    @Override
    public GameResult getResult() {
        return result;
    }

    @Override
    public int getScore(PlayerRole player) {
        return player == PlayerRole.FIRST ? scoreFirst : scoreSecond;
    }

    public FixedBoard<CheckerPiece> getBoard() {
        return board;
    }

    @Override
    public boolean makeMove(CheckersMove move) {
        if (result.isFinished()) return false;

        List<CheckersMove> legalMoves = getLegalMoves();
        if (!legalMoves.contains(move)) {
            return false;
        }

        CheckerPiece piece = board.get(move.from()).orElseThrow();
        board.clear(move.from());

        // Якщо був бій
        if (move.isCapture()) {
            board.clear(move.capturedCoord().get());
            if (currentTurn == PlayerRole.FIRST) {
                scoreFirst++;
            } else {
                scoreSecond++;
            }
        }

        // Перевірка на перетворення в дамку
        boolean becameKing = piece.isKing();
        if (!becameKing) {
            if (currentTurn == PlayerRole.FIRST && move.to().row() == 0) {
                becameKing = true;
            } else if (currentTurn == PlayerRole.SECOND && move.to().row() == size - 1) {
                becameKing = true;
            }
        }

        board.set(move.to(), becameKing ? piece.promoteToKing() : piece);

        // Перемикання ходу
        currentTurn = currentTurn.opponent();

        // Перевірка кінця гри (якщо у наступного гравця немає ходів)
        if (getLegalMoves().isEmpty()) {
            result = GameResult.win(currentTurn.opponent());
        }

        return true;
    }

    @Override
    public List<CheckersMove> getLegalMoves() {
        if (result.isFinished()) return List.of();

        List<CheckersMove> captures = new ArrayList<>();
        List<CheckersMove> simpleMoves = new ArrayList<>();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Coordinate from = new Coordinate(r, c);
                var opt = board.get(from);
                if (opt.isPresent() && opt.get().owner() == currentTurn) {
                    collectMovesForPiece(from, opt.get(), captures, simpleMoves);
                }
            }
        }

        // Правило обов'язкового взяття: якщо є бій, ходити просто так не можна
        return !captures.isEmpty() ? captures : simpleMoves;
    }

    private void collectMovesForPiece(Coordinate from, CheckerPiece piece,
                                      List<CheckersMove> captures, List<CheckersMove> simpleMoves) {
        int[] dr = {-1, -1, 1, 1};
        int[] dc = {-1, 1, -1, 1};

        int forwardDir = (piece.owner() == PlayerRole.FIRST) ? -1 : 1;

        for (int i = 0; i < 4; i++) {
            int dRow = dr[i];
            int dCol = dc[i];

            Coordinate step = from.add(dRow, dCol);
            Coordinate jump = from.add(dRow * 2, dCol * 2);

            // Перевірка бою (через одну клітинку)
            if (board.contains(jump) && board.isEmpty(jump) && board.contains(step)) {
                var middlePiece = board.get(step);
                if (middlePiece.isPresent() && middlePiece.get().owner() != piece.owner()) {
                    boolean canCapture = piece.isKing() || canManCaptureBackwards() || (dRow == forwardDir);
                    if (canCapture) {
                        captures.add(new CheckersMove(from, jump, java.util.Optional.of(step)));
                    }
                }
            }

            // Простий хід (без бою)
            if (board.contains(step) && board.isEmpty(step)) {
                boolean canStep = piece.isKing() || (dRow == forwardDir);
                if (canStep) {
                    simpleMoves.add(new CheckersMove(from, step, java.util.Optional.empty()));
                }
            }
        }
    }

}
