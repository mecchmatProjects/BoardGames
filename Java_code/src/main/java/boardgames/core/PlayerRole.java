package boardgames.core;

public enum PlayerRole {
    FIRST("Гравець 1"),
        SECOND("Гравець 2");

    private final String defaultName;


    PlayerRole(String defaultName) {
        this.defaultName = defaultName;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public PlayerRole opponent() {
        return this == FIRST ? SECOND : FIRST;
    }
}
