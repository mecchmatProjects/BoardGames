package boardgames.session;

import boardgames.core.Game;
import java.util.function.Supplier;

public record GameDescriptor(
        String id,
        String title,
        String description,
        Supplier<Game<?>> factory,
        Supplier<BoardRenderer<?>> rendererFactory
) {}