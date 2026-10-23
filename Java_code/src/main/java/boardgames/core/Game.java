package boardgames.core;

import java.util.List;

public interface Game<M> {
    String getName();
    String getRules();

    PlayerRole getCurrTurn();
    GameResult getResult();

    boolean makeMove(M move);
    List<M> getLegalMoves();

    int getScore(PlayerRole player);
}
