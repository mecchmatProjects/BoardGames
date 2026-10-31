package boardgames.games.checkers;

import boardgames.core.Coordinate;
import java.util.Optional;

public record CheckersMove (Coordinate from, Coordinate to, Optional<Coordinate> capturedCoord) {
    public boolean isCapture() {
        return capturedCoord.isPresent();
    }

    @Override
    public String toString() {
        if (isCapture()) {
            return String.format("%s:x:%s", from, to);
        }
        return String.format("%s->%s", from, to);
    }
}
