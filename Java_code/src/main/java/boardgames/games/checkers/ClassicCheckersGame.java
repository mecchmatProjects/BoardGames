package boardgames.games.checkers;

public class ClassicCheckersGame extends AbstractCheckersGame {
    public ClassicCheckersGame() {
        super(8); // Дошка 8x8
    }

    @Override
    public String getName() {
        return "Класичні шашки (8х8)";
    }

    @Override
    public String getRules() {
        return "Дошка 8х8. Прості шашки ходять вперед по діагоналі, але можуть бити як вперед, так і назад. " +
                "Взяття є обов'язковим.";
    }

    @Override
    protected boolean canManCaptureBackwards() {
        return true; // У класичних шашках проста б'є назад
    }

    @Override
    protected boolean isFlyingKing() {
        return true;
    }
}
