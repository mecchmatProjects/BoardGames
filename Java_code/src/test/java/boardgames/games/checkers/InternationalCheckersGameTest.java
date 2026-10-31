package boardgames.games.checkers;

import boardgames.core.Coordinate;
import boardgames.core.PlayerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InternationalCheckersGameTest {
    private InternationalCheckersGame game;

    @BeforeEach
    void setUp() {
        game = new InternationalCheckersGame();
    }

    @Test
    @DisplayName("Розмір дошки повинен бути 10х10")
    void testBoardDimensions() {
        assertEquals("Міжнародні шашки (10х10)", game.getName());
        assertEquals(10, game.getBoard().rows());
        assertEquals(10, game.getBoard().cols());
    }

    @Test
    @DisplayName("Початкова розстановка: по 20 шашок у кожного гравця (4 ряди)")
    void testInitialPieceCount() {
        var board = game.getBoard();
        int whiteCount = 0;
        int blackCount = 0;

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                var pieceOpt = board.get(new Coordinate(r, c));
                if (pieceOpt.isPresent()) {
                    if (pieceOpt.get().owner() == PlayerRole.FIRST) {
                        whiteCount++;
                    } else {
                        blackCount++;
                    }
                }
            }
        }

        assertEquals(20, whiteCount, "У білих має бути 20 шашок на старті");
        assertEquals(20, blackCount, "У чорних має бути 20 шашок на старті");
    }

    @Test
    @DisplayName("Перетворення в дамку відбувається на 0-му рядку для білих на дошці 10х10")
    void testKingPromotionOnRowZero() {
        var board = game.getBoard();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                board.clear(new Coordinate(r, c));
            }
        }

        // Біла шашка на передостанньому ряду (1, 2) робить хід на дамковий ряд (0, 3)
        board.set(new Coordinate(1, 2), new CheckerPiece(PlayerRole.FIRST, false));

        CheckersMove promotionMove = new CheckersMove(
                new Coordinate(1, 2),
                new Coordinate(0, 3),
                java.util.Optional.empty()
        );

        assertTrue(game.makeMove(promotionMove));

        var promotedPiece = board.get(new Coordinate(0, 3));
        assertTrue(promotedPiece.isPresent());
        assertTrue(promotedPiece.get().isKing(), "Шашка, дійшовши до рядка 0 на дошці 10х10, має стати дамкою");
    }

    @Test
    @DisplayName("У міжнародних шашках проста шашка може бити назад")
    void testManCanCaptureBackwards() {
        var board = game.getBoard();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                board.clear(new Coordinate(r, c));
            }
        }

        // Біла шашка (FIRST) на (4, 4), чорна позаду на (5, 5)
        board.set(new Coordinate(4, 4), new CheckerPiece(PlayerRole.FIRST, false));
        board.set(new Coordinate(5, 5), new CheckerPiece(PlayerRole.SECOND, false));

        List<CheckersMove> legalMoves = game.getLegalMoves();

        // Стрибок назад на (6, 6) має бути обов'язковим
        assertEquals(1, legalMoves.size());
        CheckersMove captureMove = legalMoves.get(0);
        assertTrue(captureMove.isCapture());
        assertEquals(new Coordinate(6, 6), captureMove.to());
    }

}
