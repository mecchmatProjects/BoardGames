package tictactoe;

import boardgames.games.tictactoe.TicTacToeGame;
import boardgames.core.GameResult;
import boardgames.core.Move;
import boardgames.games.tictactoe.GameMode;


public class TicTacToeGameTest {

    public static void main(String[] args) {
        horizontalWin();
        drawWhenBoardIsFullWithoutLine();
        infiniteWinWithNegativeCoordinates();
        System.out.println("Усі сценарії виконано успішно!");
    }

    private static void play(TicTacToeGame game, int[]... moves) {
        for (int[] m : moves) {
            game.makeMove(new Move.Place(m[0], m[1]));
        }
    }

    private static int[] mv(int row, int col) {
        return new int[]{row, col};
    }

    static void horizontalWin() {
        TicTacToeGame game = new TicTacToeGame();
        play(game, mv(0, 0), mv(1, 0), mv(0, 1), mv(1, 1), mv(0, 2));
        printResult("Горизонтальна перемога", game);
    }

    static void drawWhenBoardIsFullWithoutLine() {
        TicTacToeGame game = new TicTacToeGame();
        play(game,
                mv(0, 0), mv(0, 1), mv(0, 2),
                mv(1, 1), mv(1, 0), mv(1, 2),
                mv(2, 1), mv(2, 0), mv(2, 2));
        printResult("Нічия", game);
    }

    static void infiniteWinWithNegativeCoordinates() {
        TicTacToeGame game = new TicTacToeGame(GameMode.INFINITE);
        play(game,
                mv(-1, -1), mv(5, 5),
                mv(-1, 0), mv(5, 6),
                mv(-1, 1), mv(5, 7),
                mv(-1, 2), mv(5, 8),
                mv(-1, 3));
        printResult("Нескінченне поле", game);
    }

    private static void printResult(String scenarioName, TicTacToeGame game) {
        GameResult result = game.getResult();
        System.out.println(scenarioName + " -> Завершено: " + result.isFinished()
                + ", Переможець: " + result.winner().map(Enum::name).orElse("Немає (нічия)"));
    }
}