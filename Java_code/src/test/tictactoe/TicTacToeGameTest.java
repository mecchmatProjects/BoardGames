package tictactoe;

import boardgames.core.GameResult;
import boardgames.core.Move;
import boardgames.core.PlayerRole;
import boardgames.games.tictactoe.TicTacToeGame;

public class TicTacToeGameTest {

    private static int failed = 0;

    public static void main(String[] args) {
        firstPlayerStartsAndTurnsAlternate();
        initialStateHasNineLegalMovesAndGameInProgress();
        rejectsOccupiedCell();
        rejectsOutOfBoundsMove();
        rejectsNullMove();
        detectsRowWin();
        detectsColumnWin();
        detectsMainDiagonalWin();
        detectsAntiDiagonalWinForSecondPlayer();
        detectsDraw();
        noMovesAllowedAfterGameFinished();
        legalMovesShrinkAfterMove();

        System.out.println();
        System.out.println(failed == 0 ? "Усі тести пройдено." : "Провалено тестів: " + failed);
    }

    // ---------- тести ----------

    private static void firstPlayerStartsAndTurnsAlternate() {
        TicTacToeGame game = new TicTacToeGame();
        check("first player starts", PlayerRole.FIRST, game.getCurrTurn());
        play(game, 0, 0);
        check("turn alternates", PlayerRole.SECOND, game.getCurrTurn());
    }

    private static void initialStateHasNineLegalMovesAndGameInProgress() {
        TicTacToeGame game = new TicTacToeGame();
        check("9 legal moves at start", 9, game.getLegalMoves().size());
        check("in progress at start", false, game.getResult().isFinished());
    }

    private static void rejectsOccupiedCell() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, 1, 1);
        check("occupied cell rejected", false, game.makeMove(new Move.Place(1, 1)));
        check("turn unchanged after rejected move", PlayerRole.SECOND, game.getCurrTurn());
    }

    private static void rejectsOutOfBoundsMove() {
        TicTacToeGame game = new TicTacToeGame();
        check("row too big rejected", false, game.makeMove(new Move.Place(3, 0)));
        check("negative row rejected", false, game.makeMove(new Move.Place(-1, 1)));
        check("col too big rejected", false, game.makeMove(new Move.Place(0, 3)));
        check("turn unchanged after out of bounds", PlayerRole.FIRST, game.getCurrTurn());
    }

    private static void rejectsNullMove() {
        TicTacToeGame game = new TicTacToeGame();
        check("null move rejected", false, game.makeMove(null));
    }

    private static void detectsRowWin() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, 0, 0, 1, 0, 0, 1, 1, 1, 0, 2);
        check("row win result", GameResult.win(PlayerRole.FIRST), game.getResult());
        check("winner score", 1, game.getScore(PlayerRole.FIRST));
        check("loser score", 0, game.getScore(PlayerRole.SECOND));
    }

    private static void detectsColumnWin() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, 0, 0, 0, 1, 1, 0, 1, 1, 2, 0);
        check("column win result", GameResult.win(PlayerRole.FIRST), game.getResult());
    }

    private static void detectsMainDiagonalWin() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, 0, 0, 0, 1, 1, 1, 0, 2, 2, 2);
        check("main diagonal win result", GameResult.win(PlayerRole.FIRST), game.getResult());
    }

    private static void detectsAntiDiagonalWinForSecondPlayer() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, 0, 0, 0, 2, 0, 1, 1, 1, 2, 2, 2, 0);
        check("anti diagonal win (second player)", GameResult.win(PlayerRole.SECOND), game.getResult());
    }

    private static void detectsDraw() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, 0, 0, 0, 1, 0, 2, 1, 1, 1, 0, 1, 2, 2, 1, 2, 0, 2, 2);
        check("draw result", GameResult.draw(), game.getResult());
        check("draw score first", 0, game.getScore(PlayerRole.FIRST));
        check("draw score second", 0, game.getScore(PlayerRole.SECOND));
    }

    private static void noMovesAllowedAfterGameFinished() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, 0, 0, 1, 0, 0, 1, 1, 1, 0, 2);
        check("move after finish rejected", false, game.makeMove(new Move.Place(2, 2)));
        check("no legal moves after finish", true, game.getLegalMoves().isEmpty());
    }

    private static void legalMovesShrinkAfterMove() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, 1, 1);
        check("8 legal moves after one move", 8, game.getLegalMoves().size());
        check("occupied cell not in legal moves", false,
                game.getLegalMoves().contains(new Move.Place(1, 1)));
    }

    // ---------- допоміжні методи ----------

    /** Виконує ходи по черзі; координати парами (рядок, стовпець), 0-based. */
    private static void play(TicTacToeGame game, int... coords) {
        for (int i = 0; i < coords.length; i += 2) {
            Move.Place move = new Move.Place(coords[i], coords[i + 1]);
            if (!game.makeMove(move)) {
                failed++;
                System.out.println("FAIL setup — хід " + move + " неочікувано відхилено");
            }
        }
    }

    private static void check(String name, Object expected, Object actual) {
        if (expected.equals(actual)) {
            System.out.println("OK   " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
            System.out.println("--- очікувалось: " + expected);
            System.out.println("--- отримано:    " + actual);
        }
    }
}