package boardgames.games;

import boardgames.core.Coordinate;
import boardgames.core.Move;
import boardgames.core.PlayerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DotsGameTest {
    private DotsGame game;

    @BeforeEach
    void setUp() {
        game = new DotsGame(10); // Використовуємо поле 10х10 для зручності тесту
    }

    @Test
    @DisplayName("Початковий стан: черга першого, рахунок 0:0")
    void testInitialState() {
        assertEquals("Точки", game.getName());
        assertEquals(PlayerRole.FIRST, game.getCurrTurn());
        assertEquals(0, game.getScore(PlayerRole.FIRST));
        assertEquals(0, game.getScore(PlayerRole.SECOND));
        assertFalse(game.getResult().isFinished());
    }

    @Test
    @DisplayName("BFS-захоплення: оточення ворожої точки приносить очко та фарбує вузол")
    void testCaptureSurroundedEnemyDot() {
        /*
           Будуємо кільце першого гравця навколо (2, 2):
           (1,2), (3,2), (2,1), (2,3)
           Ворожа точка ставиться всередину на (2, 2)
        */
        game.makeMove(new Move.Place(new Coordinate(1, 2))); // FIRST
        game.makeMove(new Move.Place(new Coordinate(2, 2))); // SECOND (жертва)

        game.makeMove(new Move.Place(new Coordinate(3, 2))); // FIRST
        game.makeMove(new Move.Place(new Coordinate(8, 8))); // SECOND (хід осторонь)

        game.makeMove(new Move.Place(new Coordinate(2, 1))); // FIRST
        game.makeMove(new Move.Place(new Coordinate(8, 9))); // SECOND (хід осторонь)

        // Фінальний замикаючий хід оточення
        game.makeMove(new Move.Place(new Coordinate(2, 3))); // FIRST

        // Перевіряємо, чи спрацювало захоплення
        assertEquals(1, game.getScore(PlayerRole.FIRST), "Перший гравець повинен отримати 1 бал за полонену точку");
        assertEquals(PlayerRole.FIRST, game.getBoard().get(new Coordinate(2, 2)).orElseThrow(),
                "Полонена точка має перефарбуватися в колір загарбника");
    }

    @Test
    @DisplayName("Точки біля краю дошки не вважаються оточеними")
    void testEdgeDotsNotCaptured() {
        // Ставимо точку на край (0, 1)
        game.makeMove(new Move.Place(new Coordinate(1, 1))); // FIRST
        game.makeMove(new Move.Place(new Coordinate(0, 1))); // SECOND (на краю)

        game.makeMove(new Move.Place(new Coordinate(0, 0))); // FIRST
        game.makeMove(new Move.Place(new Coordinate(9, 9))); // SECOND

        game.makeMove(new Move.Place(new Coordinate(0, 2))); // FIRST
        game.makeMove(new Move.Place(new Coordinate(9, 8))); // SECOND

        assertEquals(0, game.getScore(PlayerRole.FIRST), "Вихід до краю дошки захищає від полону");
    }
}
