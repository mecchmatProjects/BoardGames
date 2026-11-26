package tictactoe;

import boardgames.core.Move;
import boardgames.games.tictactoe.GameMode;
import boardgames.games.tictactoe.MoveInputParser;
import boardgames.games.tictactoe.TicTacToeGame;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoveInputParserTest {

    private final MoveInputParser parser = new MoveInputParser(new TicTacToeGame());
    private final MoveInputParser infiniteParser = new MoveInputParser(new TicTacToeGame(GameMode.INFINITE));

    private void assertRejected(MoveInputParser p, String input) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> p.parse(input));
        assertFalse(e.getMessage() == null || e.getMessage().isBlank(),
                "Повідомлення про помилку не має бути порожнім");
    }

    // ---------- скінченне поле ----------

    @Test
    void parsesSpaceSeparatedInputAsZeroBased() {
        assertEquals(new Move.Place(1, 2), parser.parse("2 3"));
    }

    @Test
    void parsesCommaSeparatedInputWithExtraSpaces() {
        assertEquals(new Move.Place(0, 0), parser.parse("  1 ,  1 "));
        assertEquals(new Move.Place(2, 1), parser.parse("3,2"));
    }

    @Test
    void parsesCornerValues() {
        assertEquals(new Move.Place(0, 0), parser.parse("1 1"));
        assertEquals(new Move.Place(2, 2), parser.parse("3 3"));
    }

    @Test
    void rejectsNullAndBlankInput() {
        assertRejected(parser, null);
        assertRejected(parser, "");
        assertRejected(parser, "   ");
    }

    @Test
    void rejectsNonNumericInput() {
        assertRejected(parser, "a b");
        assertRejected(parser, "1 b");
        assertRejected(parser, "1.5 2");
        assertRejected(parser, "99999999999 1");
    }

    @Test
    void rejectsWrongNumberOfValues() {
        assertRejected(parser, "1");
        assertRejected(parser, "1 2 3");
    }

    @Test
    void rejectsOutOfRangeCoordinates() {
        assertRejected(parser, "0 1");
        assertRejected(parser, "1 4");
        assertRejected(parser, "-1 2");
    }

    // ---------- нескінченне поле (GameMode.INFINITE) ----------

    @Test
    void infiniteParsesCoordinatesAsIs() {
        assertEquals(new Move.Place(2, 3), infiniteParser.parse("2 3"));
        assertEquals(new Move.Place(0, 0), infiniteParser.parse("0 0"));
    }

    @Test
    void infiniteAcceptsNegativeCoordinates() {
        assertEquals(new Move.Place(2, -3), infiniteParser.parse("2 -3"));
        assertEquals(new Move.Place(-1, -1), infiniteParser.parse("-1 -1"));
    }

    @Test
    void infiniteAcceptsCommaSeparatedValues() {
        assertEquals(new Move.Place(-5, 40), infiniteParser.parse(" -5 ,  40 "));
    }

    @Test
    void infiniteAcceptsLimitValues() {
        int limit = MoveInputParser.INFINITE_LIMIT;
        assertEquals(new Move.Place(limit, -limit), infiniteParser.parse(limit + " -" + limit));
    }

    @Test
    void infiniteRejectsCoordinatesBeyondLimit() {
        int over = MoveInputParser.INFINITE_LIMIT + 1;
        assertRejected(infiniteParser, over + " 0");
        assertRejected(infiniteParser, "0 -" + over);
        assertRejected(infiniteParser, "100000 100000");
        assertRejected(infiniteParser, Integer.MAX_VALUE + " " + Integer.MIN_VALUE);
    }

    @Test
    void infiniteRejectsInvalidInput() {
        assertRejected(infiniteParser, null);
        assertRejected(infiniteParser, "   ");
        assertRejected(infiniteParser, "1");
        assertRejected(infiniteParser, "1 2 3");
        assertRejected(infiniteParser, "a -1");
        assertRejected(infiniteParser, "1.5 2");
        assertRejected(infiniteParser, "99999999999 1");
    }
}