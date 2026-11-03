package boardgames.session;

import boardgames.core.Game;
import boardgames.core.GameResult;
import boardgames.core.PlayerRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameSession<M> {
    private final Game<M> game;
    private final List<M> moveHistory = new ArrayList<>();

    public GameSession(Game<M> game) {
        this.game = game;
    }

    public Game<M> getGame() {
        return game;
    }

    public boolean executeMove(M move) {
        boolean success = game.makeMove(move);
        if (success) {
            moveHistory.add(move);
        }
        return success;
    }

    public List<M> getMoveHistory() {
        return Collections.unmodifiableList(moveHistory);
    }

    public boolean isFinished() {
        return game.getResult().isFinished();
    }

    public GameResult getResult() {
        return game.getResult();
    }

    public PlayerRole getCurrentTurn() {
        return game.getCurrTurn();
    }
}