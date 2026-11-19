package boardgames.games.tictactoe;


import boardgames.games.tictactoe.TicTacToeGame;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        PrintStream utf8Out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        new TicTacToeConsoleSession(new TicTacToeGame(), System.in, utf8Out).run();
    }
}