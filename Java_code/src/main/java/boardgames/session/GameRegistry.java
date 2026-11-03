package boardgames.session;

import boardgames.games.checkers.*;
import java.util.ArrayList;
import java.util.List;

public class GameRegistry {
    private final List<GameDescriptor> registeredGames = new ArrayList<>();

    public GameRegistry() {
        // Реєстрація трьох видів шашок
        registeredGames.add(new GameDescriptor(
                "1",
                "Класичні шашки (8х8)",
                "Дошка 8х8. Прості шашки б'ють як вперед, так і назад. Обов'язкове взяття.",
                ClassicCheckersGame::new,
                CheckersConsoleRenderer::new
        ));

        registeredGames.add(new GameDescriptor(
                "2",
                "Англійські шашки (8х8)",
                "Дошка 8х8. Прості шашки ходять і б'ють тільки вперед.",
                EnglishCheckersGame::new,
                CheckersConsoleRenderer::new
        ));

        registeredGames.add(new GameDescriptor(
                "3",
                "Міжнародні шашки (10х10)",
                "Дошка 10х10, по 20 шашок у кожного гравця. Бій назад дозволено.",
                InternationalCheckersGame::new,
                CheckersConsoleRenderer::new
        ));

        // Сюди додадуться: Хрестики-нулики, Креноль, Го, Точки та інші
    }

    public List<GameDescriptor> getGames() {
        return registeredGames;
    }
}
