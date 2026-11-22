#pragma once

#include <SFML/Graphics.hpp>
#include <vector>
#include <memory>
#include <optional>
#include <string>

enum class Color {
    None,
    White,
    Black
};

enum class PieceType {
    Man,
    King
};

struct Position {
    int row;
    int col;

    bool isValid() const {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    bool operator==(const Position& other) const {
        return row == other.row && col == other.col;
    }
};

struct MoveInfo {
    Position to;
    bool isCapture;
    Position capturedPos;
};


class Piece {
private:
    Color color;
    PieceType type;

public:
    Piece(Color c, PieceType t = PieceType::Man);

    Color getColor() const { return color; }
    PieceType getType() const { return type; }
    void promote() { type = PieceType::King; }
};


class Board {
private:
    std::vector<std::vector<std::unique_ptr<Piece>>> grid;

public:
    Board();
    void initialize();

    Piece* getPiece(Position pos) const;
    bool isEmpty(Position pos) const;

    // Checks a single candidate move without enforcing global mandatory-capture rules
    bool evaluateRawMove(Position from, Position to, Color player, bool& isCapture, Position& capturedPos) const;

    // Returns legal moves for a piece, respecting mandatory capture and optional forced multi-capture piece
    std::vector<MoveInfo> getLegalMovesForPiece(Position from, Color player, std::optional<Position> forcedPiece = std::nullopt) const;

    // Checks whether the player has at least one capture move anywhere on the board
    bool hasAnyCapture(Color player) const;

    // Checks whether a specific piece currently has at least one capture move
    bool hasCaptureFrom(Position from, Color player) const;

    // Executes a move if legal. Sets continuedCapture=true if the same piece can capture again.
    bool movePiece(Position from, Position to, Color currentPlayer,
                   std::optional<Position> forcedPiece, bool& continuedCapture);

    bool hasValidMoves(Color player) const;
    int countPieces(Color player) const;
};


class Game {
private:
    static constexpr int BOARD_SIZE = 8;
    static constexpr float TILE_SIZE = 100.f;

    sf::RenderWindow window;
    Board board;
    Color currentTurn;
    bool isGameOver;
    std::optional<Position> selectedPos;
    std::optional<Position> forcedPiecePos;
    std::vector<MoveInfo> highlightedMoves;

public:
    Game();
    void run();

private:
    void processEvents();
    void handleMouseClick(int x, int y);
    void refreshHighlightedMoves();
    void render();
    void switchTurn();
    void updateWindowTitle();
    void resetGame();
};