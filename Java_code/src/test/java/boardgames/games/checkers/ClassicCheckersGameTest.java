package boardgames.games.checkers;

import boardgames.core.Coordinate;
import boardgames.core.PlayerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ClassicCheckersGameTest {
    private ClassicCheckersGame game;

    @BeforeEach
    void setUp() {
        game = new ClassicCheckersGame();
    }

    @Test
    @DisplayName("Початковий стан: хід першого гравця, рахунок 0:0, гра не завершена")
    void testInitialState() {
        assertEquals(PlayerRole.FIRST, game.getCurrTurn());
        assertEquals(0, game.getScore(PlayerRole.FIRST));
        assertEquals(0, game.getScore(PlayerRole.SECOND));
        assertFalse(game.getResult().isFinished());
    }

    @Test
    @DisplayName("Початкова розстановка: шашки стоять тільки на темних клітинках")
    void testBoardSetup() {
        var board = game.getBoard();
        // Рядки 0-2: чорні (SECOND)
        assertTrue(board.get(new Coordinate(0, 1)).isPresent());
        assertEquals(PlayerRole.SECOND, board.get(new Coordinate(0, 1)).get().owner());

        // Рядки 5-7: білі (FIRST)
        assertTrue(board.get(new Coordinate(5, 0)).isPresent());
        assertEquals(PlayerRole.FIRST, board.get(new Coordinate(5, 0)).get().owner());

        // Світла клітинка має бути порожньою
        assertTrue(board.get(new Coordinate(0, 0)).isEmpty());
    }

    @Test
    @DisplayName("makeMove виконує коректний тихий хід та перемикає чергу")
    void testMakeMoveValidSimple() {
        Coordinate from = new Coordinate(5, 0);
        Coordinate to = new Coordinate(4, 1);
        CheckersMove move = new CheckersMove(from, to, Optional.empty());

        assertTrue(game.makeMove(move));
        assertTrue(game.getBoard().isEmpty(from));
        assertTrue(game.getBoard().get(to).isPresent());
        assertEquals(PlayerRole.SECOND, game.getCurrTurn());
    }

    @Test
    @DisplayName("makeMove відхиляє некоректний хід (наприклад, стрибок через клітинку без взяття)")
    void testMakeMoveInvalid() {
        Coordinate from = new Coordinate(5, 0);
        Coordinate invalidTo = new Coordinate(3, 0);
        CheckersMove move = new CheckersMove(from, invalidTo, Optional.empty());

        assertFalse(game.makeMove(move));
        assertEquals(PlayerRole.FIRST, game.getCurrTurn()); // Хід не мав змінитись
    }

    @Test
    @DisplayName("Правило обов'язкового взяття: якщо є можливість бити, звичайні ходи недоступні")
    void testMandatoryCaptureRule() {
        // Очистимо дошку для створення штучної ситуації взяття
        var board = game.getBoard();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board.clear(new Coordinate(r, c));
            }
        }

        // Біла шашка на (4, 4), Чорна на (3, 3)
        board.set(new Coordinate(4, 4), new CheckerPiece(PlayerRole.FIRST, false));
        board.set(new Coordinate(3, 3), new CheckerPiece(PlayerRole.SECOND, false));

        List<CheckersMove> legalMoves = game.getLegalMoves();

        // Повинен бути лише один обов'язковий хід — стрибок на (2, 2) зі збиттям (3, 3)
        assertEquals(1, legalMoves.size());
        CheckersMove captureMove = legalMoves.get(0);
        assertTrue(captureMove.isCapture());
        assertEquals(new Coordinate(2, 2), captureMove.to());

        // Виконуємо взяття
        assertTrue(game.makeMove(captureMove));
        // Збита шашка має зникнути
        assertTrue(board.isEmpty(new Coordinate(3, 3)));
        // Рахунок гравця має збільшитись
        assertEquals(1, game.getScore(PlayerRole.FIRST));
    }

}
