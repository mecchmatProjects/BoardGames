package boardgames.games.tictactoe;

/**
 * Доступні режими гри: розмір поля та довжина виграшної лінії.
 */
public enum GameMode {
    CLASSIC(3, 3),
    MEDIUM(8, 5),
    LARGE(10, 5);

    private final int size;
    private final int winLength;

    GameMode(int size, int winLength) {
        this.size = size;
        this.winLength = winLength;
    }

    public int getSize() {
        return size;
    }

    public int getWinLength() {
        return winLength;
    }

    public String getLabel() {
        return size + " на " + size + " (лінія з " + winLength + ")";
    }
}