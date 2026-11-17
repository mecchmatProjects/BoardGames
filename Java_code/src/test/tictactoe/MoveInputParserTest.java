package tictactoe;

import boardgames.core.Move;
import boardgames.games.tictactoe.MoveInputParser;

public class MoveInputParserTest {

    private static final MoveInputParser PARSER = new MoveInputParser(3);
    private static int failed = 0;

    public static void main(String[] args) {
        parsesSpaceSeparatedInputAsZeroBased();
        parsesCommaSeparatedInputWithExtraSpaces();
        parsesCornerValues();
        rejectsNullAndBlankInput();
        rejectsNonNumericInput();
        rejectsWrongNumberOfValues();
        rejectsOutOfRangeCoordinates();

        System.out.println();
        System.out.println(failed == 0 ? "Усі тести пройдено." : "Провалено тестів: " + failed);
    }

    private static void parsesSpaceSeparatedInputAsZeroBased() {
        check("space separated", new Move.Place(1, 2), PARSER.parse("2 3"));
    }

    private static void parsesCommaSeparatedInputWithExtraSpaces() {
        check("comma separated", new Move.Place(0, 0), PARSER.parse("  1 ,  1 "));
        check("comma without spaces", new Move.Place(2, 1), PARSER.parse("3,2"));
    }

    private static void parsesCornerValues() {
        check("min corner", new Move.Place(0, 0), PARSER.parse("1 1"));
        check("max corner", new Move.Place(2, 2), PARSER.parse("3 3"));
    }

    private static void rejectsNullAndBlankInput() {
        checkRejected("null", null);
        checkRejected("empty", "");
        checkRejected("blank", "   ");
    }

    private static void rejectsNonNumericInput() {
        checkRejected("letters", "a b");
        checkRejected("one letter", "1 b");
        checkRejected("decimal", "1.5 2");
        checkRejected("too big number", "99999999999 1");
    }

    private static void rejectsWrongNumberOfValues() {
        checkRejected("one value", "1");
        checkRejected("three values", "1 2 3");
    }

    private static void rejectsOutOfRangeCoordinates() {
        checkRejected("row zero", "0 1");
        checkRejected("col too big", "1 4");
        checkRejected("negative", "-1 2");
    }

    private static void check(String name, Object expected, Object actual) {
        if (expected.equals(actual)) {
            System.out.println("OK   " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name);
            System.out.println("--- очікувалось: " + expected);
            System.out.println("--- отримано:    " + actual);
        }
    }

    /** Перевіряє, що парсер кидає IllegalArgumentException з непорожнім повідомленням. */
    private static void checkRejected(String name, String input) {
        try {
            Move.Place result = PARSER.parse(input);
            failed++;
            System.out.println("FAIL " + name + " — очікувався виняток, отримано " + result);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() == null || e.getMessage().isBlank()) {
                failed++;
                System.out.println("FAIL " + name + " — порожнє повідомлення про помилку");
            } else {
                System.out.println("OK   " + name + "  →  " + e.getMessage());
            }
        }
    }
}