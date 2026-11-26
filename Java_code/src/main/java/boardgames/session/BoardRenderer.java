package boardgames.session;

import boardgames.core.Game;

public interface BoardRenderer<M> {
    void render(Game<M> game);
}
