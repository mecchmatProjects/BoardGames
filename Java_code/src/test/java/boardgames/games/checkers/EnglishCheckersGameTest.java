package boardgames.games.checkers;

import boardgames.core.Coordinate;
import boardgames.core.PlayerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EnglishCheckersGameTest {
    private EnglishCheckersGame game;

    @BeforeEach
    void setUp() {
        game = new EnglishCheckersGame();
    }

    @Test
    @DisplayName("Метадані: розмір дошки 8х8 та правильна назва")
    void testMetadata() {
        assertEquals("Англійські шашки (Checkers)", game.getName());
        assertEquals(8, game.getBoard().rows());
        assertEquals(8, game.getBoard().cols());
    }

    @Test
    @DisplayName("Проста шашка НЕ може бити назад (заборона взяття назад)")
    void testManCannotCaptureBackwards() {
        var board = game.getBoard();
        // Очищаємо поле для ізольованого тесту
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board.clear(new Coordinate(r, c));
            }
        }

        // Біла шашка (FIRST) на (3, 3). Хід білих — знизу вгору (рядки зменшуються)
        // Ставимо чорну шашку (SECOND) ПОЗАДУ білої: на (4, 4)
        board.set(new Coordinate(3, 3), new CheckerPiece(PlayerRole.FIRST, false));
        board.set(new Coordinate(4, 4), new CheckerPiece(PlayerRole.SECOND, false));

        List<CheckersMove> legalMoves = game.getLegalMoves();

        // Стрибок назад на (5, 5) має бути відсутній
        boolean hasBackwardCapture = legalMoves.stream()
                .anyMatch(m -> m.isCapture() && m.to().equals(new Coordinate(5, 5)));

        assertFalse(hasBackwardCapture, "В англійських шашках проста шашка не повинна бити назад");
    }

    @Test
    @DisplayName("Проста шашка може бити вперед")
    void testManCanCaptureForward() {
        var board = game.getBoard();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board.clear(new Coordinate(r, c));
            }
        }

        // Біла шашка (FIRST) на (4, 4), чорна ПОПЕРЕДУ на (3, 3)
        board.set(new Coordinate(4, 4), new CheckerPiece(PlayerRole.FIRST, false));
        board.set(new Coordinate(3, 3), new CheckerPiece(PlayerRole.SECOND, false));

        List<CheckersMove> legalMoves = game.getLegalMoves();

        // Повинен бути бій вперед на (2, 2)
        assertEquals(1, legalMoves.size());
        CheckersMove move = legalMoves.get(0);
        assertTrue(move.isCapture());
        assertEquals(new Coordinate(2, 2), move.to());
    }
}
