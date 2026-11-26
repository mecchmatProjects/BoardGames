package tictactoe;

import boardgames.core.Coordinate;
import boardgames.core.Move;
import boardgames.core.PlayerRole;
import boardgames.games.tictactoe.GameMode;
import boardgames.games.tictactoe.TicTacToeGame;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicTacToeGameTest {

    /** Виконує ходи по черзі (перший хід — X) і перевіряє, що кожен прийнято. */
    private static void play(TicTacToeGame game, int[]... moves) {
        for (int[] m : moves) {
            assertTrue(game.makeMove(new Move.Place(m[0], m[1])),
                    "Хід мав бути прийнятий: " + m[0] + "," + m[1]);
        }
    }

    private static int[] mv(int row, int col) {
        return new int[]{row, col};
    }

    private static void assertWinner(TicTacToeGame game, PlayerRole expected) {
        assertTrue(game.getResult().isFinished());
        assertEquals(expected, game.getResult().winner().orElseThrow());
    }

    // ---------- перемоги на класичному полі ----------

    @Test
    void horizontalWin() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, mv(0, 0), mv(1, 0), mv(0, 1), mv(1, 1), mv(0, 2));
        assertWinner(game, PlayerRole.FIRST);
    }

    @Test
    void verticalWin() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, mv(0, 0), mv(0, 1), mv(1, 0), mv(1, 1), mv(2, 0));
        assertWinner(game, PlayerRole.FIRST);
    }

    @Test
    void mainDiagonalWin() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, mv(0, 0), mv(0, 1), mv(1, 1), mv(0, 2), mv(2, 2));
        assertWinner(game, PlayerRole.FIRST);
    }

    @Test
    void antiDiagonalWin() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, mv(0, 2), mv(0, 0), mv(1, 1), mv(0, 1), mv(2, 0));
        assertWinner(game, PlayerRole.FIRST);
    }

    @Test
    void secondPlayerCanWin() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, mv(0, 0), mv(1, 0), mv(0, 1), mv(1, 1), mv(2, 2), mv(1, 2));
        assertWinner(game, PlayerRole.SECOND);
    }

    @Test
    void drawWhenBoardIsFullWithoutLine() {
        TicTacToeGame game = new TicTacToeGame();
        play(game,
                mv(0, 0), mv(0, 1), mv(0, 2),
                mv(1, 1), mv(1, 0), mv(1, 2),
                mv(2, 1), mv(2, 0), mv(2, 2));
        assertTrue(game.getResult().isFinished());
        assertTrue(game.getResult().isDraw());
        assertTrue(game.getResult().winner().isEmpty());
    }

    @Test
    void gameIsInProgressAtStart() {
        TicTacToeGame game = new TicTacToeGame();
        assertFalse(game.getResult().isFinished());
        assertEquals(PlayerRole.FIRST, game.getCurrTurn());
    }

    @Test
    void turnAlternatesAfterEachMove() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, mv(0, 0));
        assertEquals(PlayerRole.SECOND, game.getCurrTurn());
        play(game, mv(1, 1));
        assertEquals(PlayerRole.FIRST, game.getCurrTurn());
    }

    // ---------- режими MEDIUM (8x8) та LARGE (10x10) ----------

    @Test
    void mediumModeHasEightByEightBoardAndNeedsFive() {
        TicTacToeGame game = new TicTacToeGame(GameMode.MEDIUM);
        assertEquals(8, game.getSize());
        assertFalse(game.isInfinite());
        assertEquals(64, game.getLegalMoves().size());

        // X: (0,0)..(0,3), O: (1,0)..(1,3) — по 4 в ряд, ще не перемога
        play(game, mv(0, 0), mv(1, 0), mv(0, 1), mv(1, 1), mv(0, 2), mv(1, 2), mv(0, 3), mv(1, 3));
        assertFalse(game.getResult().isFinished());

        play(game, mv(0, 4));
        assertWinner(game, PlayerRole.FIRST);
    }

    @Test
    void mediumModeWinsOnDiagonal() {
        TicTacToeGame game = new TicTacToeGame(GameMode.MEDIUM);
        play(game,
                mv(0, 0), mv(0, 1), mv(1, 1), mv(0, 2), mv(2, 2),
                mv(0, 3), mv(3, 3), mv(0, 5), mv(4, 4));
        assertWinner(game, PlayerRole.FIRST);
    }

    @Test
    void largeModeHasTenByTenBoardAndNeedsFive() {
        TicTacToeGame game = new TicTacToeGame(GameMode.LARGE);
        assertEquals(10, game.getSize());
        assertEquals(100, game.getLegalMoves().size());

        play(game, mv(0, 9), mv(9, 0), mv(1, 9), mv(8, 0), mv(2, 9), mv(7, 0), mv(3, 9), mv(6, 0));
        assertFalse(game.getResult().isFinished());

        play(game, mv(4, 9));
        assertWinner(game, PlayerRole.FIRST);
    }

    @Test
    void largeModeDoesNotWinWithFourInLine() {
        TicTacToeGame game = new TicTacToeGame(GameMode.LARGE);
        play(game, mv(5, 5), mv(0, 0), mv(5, 6), mv(0, 2), mv(5, 7), mv(0, 4), mv(5, 8));
        assertFalse(game.getResult().isFinished());
    }

    // ---------- нескінченне поле ----------

    @Test
    void infiniteWinWithNegativeCoordinates() {
        TicTacToeGame game = new TicTacToeGame(GameMode.INFINITE);
        assertTrue(game.isInfinite());
        play(game,
                mv(-1, -1), mv(5, 5),
                mv(-1, 0), mv(5, 6),
                mv(-1, 1), mv(5, 7),
                mv(-1, 2), mv(5, 8),
                mv(-1, 3));
        assertWinner(game, PlayerRole.FIRST);
    }

    @Test
    void infiniteBoardNeverEndsInDraw() {
        TicTacToeGame game = new TicTacToeGame(GameMode.INFINITE);
        play(game, mv(0, 0), mv(0, 2), mv(1, 1));
        assertFalse(game.getResult().isFinished());
    }

    // ---------- заборонені ходи ----------

    @Test
    void rejectsMoveToOccupiedCell() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, mv(1, 1));
        assertFalse(game.makeMove(new Move.Place(1, 1)));
        // чия черга та кількість зайнятих клітинок не змінилися
        assertEquals(PlayerRole.SECOND, game.getCurrTurn());
        assertEquals(1, game.getOccupiedCoordinates().size());
    }

    @Test
    void rejectsMoveAfterGameIsFinished() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, mv(0, 0), mv(1, 0), mv(0, 1), mv(1, 1), mv(0, 2));
        assertTrue(game.getResult().isFinished());

        assertFalse(game.makeMove(new Move.Place(2, 2)));
        assertTrue(game.getCellOwner(new Coordinate(2, 2)).isEmpty());
    }

    @Test
    void rejectsMoveOutsideFixedBoard() {
        TicTacToeGame game = new TicTacToeGame();
        assertFalse(game.makeMove(new Move.Place(3, 0)));
        assertFalse(game.makeMove(new Move.Place(0, -1)));
        assertEquals(PlayerRole.FIRST, game.getCurrTurn());
        assertTrue(game.getOccupiedCoordinates().isEmpty());
    }

    @Test
    void rejectsNullMove() {
        assertFalse(new TicTacToeGame().makeMove(null));
    }

    // ---------- getLegalMoves ----------

    @Test
    void legalMovesShrinkAfterEachMove() {
        TicTacToeGame game = new TicTacToeGame();
        assertEquals(9, game.getLegalMoves().size());
        play(game, mv(1, 1));
        List<Move.Place> moves = game.getLegalMoves();
        assertEquals(8, moves.size());
        assertFalse(moves.contains(new Move.Place(1, 1)));
    }

    @Test
    void legalMovesEmptyAfterGameFinished() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, mv(0, 0), mv(1, 0), mv(0, 1), mv(1, 1), mv(0, 2));
        assertTrue(game.getLegalMoves().isEmpty());
    }

    @Test
    void infiniteLegalMovesStartAtOrigin() {
        TicTacToeGame game = new TicTacToeGame(GameMode.INFINITE);
        assertEquals(List.of(new Move.Place(0, 0)), game.getLegalMoves());
    }

    @Test
    void infiniteLegalMovesAreNeighboursOfOccupiedCells() {
        TicTacToeGame game = new TicTacToeGame(GameMode.INFINITE);
        play(game, mv(0, 0));
        List<Move.Place> moves = game.getLegalMoves();
        assertEquals(8, moves.size());
        assertFalse(moves.contains(new Move.Place(0, 0)));
        assertTrue(moves.contains(new Move.Place(-1, -1)));
        assertTrue(moves.contains(new Move.Place(1, 1)));
    }

    // ---------- getScore ----------

    @Test
    void scoreIsOneForWinnerAndZeroForLoser() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, mv(0, 0), mv(1, 0), mv(0, 1), mv(1, 1), mv(0, 2));
        assertEquals(1, game.getScore(PlayerRole.FIRST));
        assertEquals(0, game.getScore(PlayerRole.SECOND));
    }

    @Test
    void scoreIsZeroForBothOnDrawAndInProgress() {
        TicTacToeGame game = new TicTacToeGame();
        assertEquals(0, game.getScore(PlayerRole.FIRST));
        assertEquals(0, game.getScore(PlayerRole.SECOND));

        play(game,
                mv(0, 0), mv(0, 1), mv(0, 2),
                mv(1, 1), mv(1, 0), mv(1, 2),
                mv(2, 1), mv(2, 0), mv(2, 2));
        assertEquals(0, game.getScore(PlayerRole.FIRST));
        assertEquals(0, game.getScore(PlayerRole.SECOND));
    }

    // ---------- валідація конструктора ----------

    @Test
    void constructorRejectsInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> new TicTacToeGame(2, 3));
        assertThrows(IllegalArgumentException.class, () -> new TicTacToeGame(3, 4));
        assertThrows(IllegalArgumentException.class, () -> new TicTacToeGame(5, 2));
        assertThrows(IllegalArgumentException.class, () -> new TicTacToeGame(0, 2));
    }
}