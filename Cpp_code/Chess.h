#pragma once

#include <SFML/Graphics.hpp>

#include <atomic>
#include <mutex>
#include <optional>
#include <string>
#include <thread>
#include <vector>

#ifdef _WIN32
#include <windows.h>
#else
#include <sys/types.h>
#include <unistd.h>
#endif

enum class ChessColor {
    None,
    White,
    Black
};

enum class PieceType {
    Pawn,
    Rook,
    Knight,
    Bishop,
    Queen,
    King
};

inline ChessColor oppositeColor(ChessColor color) {
    return color == ChessColor::White ? ChessColor::Black : ChessColor::White;
}

inline bool isWhite(ChessColor color) { return color == ChessColor::White; }

struct ChessSquare {
    int row; // 0 = rank 8 (top, black back rank), 7 = rank 1 (bottom, white back rank)
    int col; // 0 = file a, 7 = file h

    bool isValid() const {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    bool operator==(const ChessSquare& other) const {
        return row == other.row && col == other.col;
    }
};

struct ChessPiece {
    ChessColor color = ChessColor::None;
    PieceType type = PieceType::Pawn;

    bool isEmpty() const { return color == ChessColor::None; }
};

struct ChessMove {
    ChessSquare from;
    ChessSquare to;
    PieceType promotion = PieceType::Pawn; // only meaningful when isPromotion is true
    bool isPromotion = false;
    bool isCastling = false;
    bool isEnPassant = false;
    bool isCapture = false;
    std::string uci; // e.g. "e2e4", "e7e8q"

    bool operator==(const ChessMove& other) const {
        return from == other.from && to == other.to;
    }
};

inline std::string squareToAlgebraic(ChessSquare square) {
    return std::string(1, static_cast<char>('a' + square.col)) + char('1' + (7 - square.row));
}


// Board (pure logic, no rendering)


class ChessBoard {
private:
    ChessPiece grid[8][8];
    // [colorIndex 0=White, 1=Black][sideIndex 0=KingSide, 1=QueenSide]
    bool castlingRights[2][2] = {{true, true}, {true, true}};
    // En passant target square (the square the pawn skipped over) or None.
    std::optional<ChessSquare> enPassantSquare;
    // Halfmove clock: plies since the last pawn move or capture.
    int halfMoveClock = 0;

    void movePiece(const ChessMove& move); // mutates in place
    bool kingAttacked(ChessColor color) const;

public:
    ChessBoard();

    void initialize();

    const ChessPiece* getPiece(ChessSquare pos) const {
        if (!pos.isValid()) return nullptr;
        return &grid[pos.row][pos.col];
    }

    std::optional<ChessSquare> getEnPassantSquare() const { return enPassantSquare; }
    int getHalfMoveClock() const { return halfMoveClock; }
    bool hasCastlingRights(ChessColor color, bool kingSide) const;

    ChessSquare findKing(ChessColor color) const;

    bool isAttacked(ChessSquare pos, ChessColor by) const;
    bool isInCheck(ChessColor color) const {
        const ChessSquare king = findKing(color);
        if (!king.isValid()) return false;
        return isAttacked(king, oppositeColor(color));
    }

    // Pseudo-legal moves (not filtered by king safety).
    std::vector<ChessMove> pseudoLegalMovesForPiece(ChessSquare from, ChessColor player) const;
    std::vector<ChessMove> pseudoLegalMoves(ChessColor player) const;

    // Fully legal moves (king-safety filtered).
    std::vector<ChessMove> legalMovesForPiece(ChessSquare from, ChessColor player) const;
    std::vector<ChessMove> legalMoves(ChessColor player) const;

    // Clone the board, apply the move, and return the new board.
    ChessBoard afterMove(const ChessMove& move) const;

    bool hasAnyLegalMove(ChessColor player) const {
        return !legalMoves(player).empty();
    }

    bool isFiftyMoveDraw() const { return halfMoveClock >= 100; }
    bool isInsufficientMaterial() const;
};


// Stockfish UCI wrapper (background thread)


enum class EngineStatus {
    Idle,
    Thinking,
    Done,
    Error   // the engine failed to start/communicate
};

class StockfishEngine {
public:
    explicit StockfishEngine(std::string exePath);
    ~StockfishEngine();

    StockfishEngine(const StockfishEngine&) = delete;
    StockfishEngine& operator=(const StockfishEngine&) = delete;

    // Starts a background search. `positionArg` is the full position string
    // (e.g. "position startpos moves e2e4 e7e5").
    void requestMove(const std::string& positionArg, int movetimeMs = 1000);

    // Poll for the result. Done -> bestMoveOut filled; Error -> errorOut filled.
    EngineStatus poll(std::string& bestMoveOut, std::string& errorOut);

    // Kill the background worker and the engine process if running.
    void cancel();

    const std::string& exePath() const { return exePath; }
    void setExePath(const std::string& path); // cancels any active search

private:
    std::string exePath;

#ifdef _WIN32
    void* processHandle = nullptr;
    void* threadHandle = nullptr;
    void* stdinWrite = nullptr;
    void* stdoutRead = nullptr;
#else
    pid_t processHandle = -1;
    void* stdinWrite = nullptr;    // fd for writing into the engine's stdin
    void* stdoutRead = nullptr;    // fd for reading the engine's stdout
#endif

    std::thread worker;
    std::atomic<EngineStatus> status{EngineStatus::Idle};
    std::string bestMove;
    std::string error;
    std::mutex mutex;

    bool spawnProcess(const std::string& positionArg);
    void runWorker(const std::string& positionArg, int movetimeMs);
    void shutdown();
};


// Game (rendering + input + engine)


class ChessGame {
private:
    static constexpr int BOARD_SIZE = 8;
    static constexpr float TILE_SIZE = 75.0f;
    static constexpr float STATUS_HEIGHT = 60.0f;
    static constexpr int BOARD_PIXELS = BOARD_SIZE * TILE_SIZE;

    sf::RenderWindow window;
    sf::Font font;
    bool fontLoaded = false;

    ChessBoard board;
    ChessColor currentTurn;
    bool isGameOver;
    std::string gameOverReason;
    std::optional<ChessSquare> selectedPos;
    std::vector<ChessMove> highlightedMoves;
    std::vector<std::string> moveHistory;   // UCI moves (for the engine)
    std::optional<ChessMove> lastMove;

    // Stockfish bot (optional)
    bool botMode = false;       // toggled with 'B'
    StockfishEngine engine;
    bool engineThinking = false;
    std::string engineError;
    bool enginePathInput = false;   // 'F' opens typed engine-path input
    std::string enginePathBuffer;

    void processEvents();
    void handleMouseClick(int x, int y);
    void refreshHighlightedMoves();
    void render();
    void updateWindowTitle();
    void resetGame();

    // Bot helpers
    void applyChosenMove(const ChessMove& move);
    void startEngineIfNeeded();
    void pollEngine();
    void applyEngineMove(const std::string& bestMove);

    // Rendering helpers
    void drawStatusBar(const std::string& statusText);
    void drawBoard();
    void drawPiece(const ChessSquare& square, const ChessPiece& piece);
    void drawMoveHighlights();
    void drawLastMoveHighlight();

    std::string buildPositionArgument() const;
    std::string currentStatusText() const;
    void checkGameOver();

public:
    ChessGame();
    void run();
};
