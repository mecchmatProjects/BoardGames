package boardgames.games.checkers;

public class InternationalCheckersGame extends AbstractCheckersGame{
    public InternationalCheckersGame() { super(10); }
    @Override public String getName() { return "Міжнародні шашки (10х10)"; }
    @Override public String getRules() { return "Дошка 10х10. Бій назад дозволено."; }
    @Override protected boolean canManCaptureBackwards() { return true; }
    @Override protected boolean isFlyingKing() { return true; }
}
