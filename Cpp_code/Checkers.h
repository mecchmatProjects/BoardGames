#ifndef CHECKERS_H
#define CHECKERS_H

#include <array>
#include <optional>
#include <vector>

namespace checkers {

enum class Player {
    Red,
    Black
};

struct Position {
    int row;
    int column;

    bool operator==(const Position& other) const;
};

struct Move {
    Position from;
    Position to;
    std::optional<Position> captured;

    bool operator==(const Move& other) const;
};

class Game {
public:
    Game();

    const std::array<std::array<char, 8>, 8>& board() const;
    Player currentPlayer() const;
    std::optional<Player> winner() const;
    bool gameOver() const;
    bool isForcedCapture() const;
    std::optional<Position> forcedPiece() const;

    std::vector<Move> legalMoves() const;
    std::vector<Move> legalMovesFrom(Position from) const;
    bool makeMove(const Move& move);

    static std::optional<Position> parsePosition(const char* text);
    static char playerName(Player player);

private:
    std::array<std::array<char, 8>, 8> board_{};
    Player currentPlayer_ = Player::Red;
    std::optional<Position> forcedPiece_;
    std::optional<Player> winner_;

    std::vector<Move> movesForPiece(Position from, bool capturesOnly) const;
    bool hasPieces(Player player) const;
    bool isPlayerPiece(char piece, Player player) const;
    bool isKing(char piece) const;
    bool isInsideBoard(Position position) const;
    void updateWinner();
};

}  // namespace checkers

#endif
