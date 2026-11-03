package boardgames.games.checkers;

public class EnglishCheckersGame extends AbstractCheckersGame{
    public EnglishCheckersGame() { super(8); }
    @Override public String getName() { return "Англійські шашки (Checkers)"; }
    @Override public String getRules() { return "Дошка 8x8. Шашки ходять і б'ють тільки вперед."; }
    @Override protected boolean canManCaptureBackwards() { return false; }
    @Override protected boolean isFlyingKing() { return false; }
}
