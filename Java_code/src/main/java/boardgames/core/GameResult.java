package boardgames.core;
import java.util.Optional;


public record GameResult(boolean isFinished, Optional<PlayerRole> winner, boolean isDraw){
    public static GameResult inProgress() {
        return new GameResult(false, Optional.empty(), false);
    }

    public static GameResult win(PlayerRole winner) {
        return new GameResult(true, Optional.of(winner), false);
    }

    public static GameResult draw() {
        return new GameResult(true, Optional.empty(), true);
    }
}
