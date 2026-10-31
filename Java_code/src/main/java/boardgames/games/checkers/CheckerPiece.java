package boardgames.games.checkers;

import boardgames.core.PlayerRole;

///шашки можуть бути звичайними, або дамкою (king)
public record CheckerPiece (PlayerRole owner, boolean isKing){
    public CheckerPiece promoteToKing(){
        return new CheckerPiece(owner, true);
    }

    @Override
    public String toString() {
        if (owner == PlayerRole.FIRST) {
            return isKing ? "W" : "w"; // w - white man, W - white king
        } else {
            return isKing ? "B" : "b"; // b - black man, B - black king
        }
    }
}
