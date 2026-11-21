#include "Checkers.h"

#include <algorithm>
#include <cctype>
#include <string>

namespace checkers {

namespace {

constexpr int BoardSize = 8;

}  // namespace

bool Position::operator==(const Position& other) const {
    return row == other.row && column == other.column;
}

bool Move::operator==(const Move& other) const {
    return from == other.from && to == other.to && captured == other.captured;
}

Game::Game() {
    for (auto& row : board_) {
        row.fill('.');
    }

    for (int row = 0; row < 3; ++row) {
        for (int column = 0; column < BoardSize; ++column) {
            if ((row + column) % 2 == 1) {
                board_[row][column] = 'b';
            }
        }
    }

    for (int row = 5; row < BoardSize; ++row) {
        for (int column = 0; column < BoardSize; ++column) {
            if ((row + column) % 2 == 1) {
                board_[row][column] = 'r';
            }
        }
    }
}

const std::array<std::array<char, 8>, 8>& Game::board() const {
    return board_;
}

Player Game::currentPlayer() const {
    return currentPlayer_;
}

std::optional<Player> Game::winner() const {
    return winner_;
}

bool Game::gameOver() const {
    return winner_.has_value();
}

bool Game::isForcedCapture() const {
    return forcedPiece_.has_value();
}

std::optional<Position> Game::forcedPiece() const {
    return forcedPiece_;
}

std::vector<Move> Game::legalMoves() const {
    std::vector<Move> captures;
    std::vector<Move> regularMoves;

    for (int row = 0; row < BoardSize; ++row) {
        for (int column = 0; column < BoardSize; ++column) {
            const Position from{row, column};
            if (!isPlayerPiece(board_[row][column], currentPlayer_)) {
                continue;
            }

            for (const Move& move : movesForPiece(from, false)) {
                if (move.captured.has_value()) {
                    captures.push_back(move);
                } else {
                    regularMoves.push_back(move);
                }
            }
        }
    }

    return captures.empty() ? regularMoves : captures;
}

std::vector<Move> Game::legalMovesFrom(Position from) const {
    if (!isInsideBoard(from) ||
        !isPlayerPiece(board_[from.row][from.column], currentPlayer_)) {
        return {};
    }

    if (forcedPiece_.has_value() && !(from == *forcedPiece_)) {
        return {};
    }

    std::vector<Move> moves = movesForPiece(from, false);
    const std::vector<Move> allMoves = legalMoves();
    const bool capturesRequired = !allMoves.empty() && allMoves.front().captured.has_value();

    if (capturesRequired || forcedPiece_.has_value()) {
        moves.erase(
            std::remove_if(moves.begin(), moves.end(), [](const Move& move) {
                return !move.captured.has_value();
            }),
            moves.end());
    }

    return moves;
}

bool Game::makeMove(const Move& move) {
    const std::vector<Move> moves = legalMovesFrom(move.from);
    bool isLegal = false;
    for (const Move& legalMove : moves) {
        if (legalMove == move) {
            isLegal = true;
            break;
        }
    }
    if (!isLegal) {
        return false;
    }

    char piece = board_[move.from.row][move.from.column];
    board_[move.from.row][move.from.column] = '.';
    if (move.captured.has_value()) {
        board_[move.captured->row][move.captured->column] = '.';
    }

    const bool reachedKingRow =
        (piece == 'r' && move.to.row == 0) || (piece == 'b' && move.to.row == BoardSize - 1);
    if (reachedKingRow) {
        piece = static_cast<char>(std::toupper(static_cast<unsigned char>(piece)));
    }
    board_[move.to.row][move.to.column] = piece;

    if (move.captured.has_value() && !reachedKingRow) {
        const std::vector<Move> nextCaptures = movesForPiece(move.to, true);
        if (!nextCaptures.empty()) {
            forcedPiece_ = move.to;
            return true;
        }
    }

    forcedPiece_.reset();
    currentPlayer_ = currentPlayer_ == Player::Red ? Player::Black : Player::Red;
    updateWinner();
    return true;
}

std::optional<Position> Game::parsePosition(const char* text) {
    if (text == nullptr) {
        return std::nullopt;
    }

    std::string coordinate(text);
    if (coordinate.size() != 2) {
        return std::nullopt;
    }

    const char column = static_cast<char>(
        std::tolower(static_cast<unsigned char>(coordinate[0])));
    const char row = coordinate[1];
    if (column < 'a' || column > 'h' || row < '1' || row > '8') {
        return std::nullopt;
    }

    return Position{BoardSize - (row - '0'), column - 'a'};
}

char Game::playerName(Player player) {
    return player == Player::Red ? 'R' : 'B';
}

std::vector<Move> Game::movesForPiece(Position from, bool capturesOnly) const {
    std::vector<Move> moves;
    if (!isInsideBoard(from) || board_[from.row][from.column] == '.') {
        return moves;
    }

    const char piece = board_[from.row][from.column];
    const int forward = std::tolower(static_cast<unsigned char>(piece)) == 'r' ? -1 : 1;
    const int rowDirections[] = {forward, -forward};
    const int directionCount = isKing(piece) ? 2 : 1;
    const int columnDirections[] = {-1, 1};

    for (int rowIndex = 0; rowIndex < directionCount; ++rowIndex) {
        for (int columnDirection : columnDirections) {
            const int rowDirection = rowDirections[rowIndex];
            const Position adjacent{from.row + rowDirection, from.column + columnDirection};
            const Position landing{from.row + 2 * rowDirection,
                                   from.column + 2 * columnDirection};

            if (isInsideBoard(adjacent) && isInsideBoard(landing) &&
                board_[landing.row][landing.column] == '.' &&
                !isPlayerPiece(board_[adjacent.row][adjacent.column], currentPlayer_)) {
                const char jumpedPiece = board_[adjacent.row][adjacent.column];
                if (jumpedPiece != '.') {
                    moves.push_back(Move{from, landing, adjacent});
                }
            }

            if (!capturesOnly && isInsideBoard(adjacent) &&
                board_[adjacent.row][adjacent.column] == '.') {
                moves.push_back(Move{from, adjacent, std::nullopt});
            }
        }
    }

    return moves;
}

bool Game::hasPieces(Player player) const {
    for (const auto& row : board_) {
        for (char piece : row) {
            if (isPlayerPiece(piece, player)) {
                return true;
            }
        }
    }
    return false;
}

bool Game::isPlayerPiece(char piece, Player player) const {
    if (piece == '.') {
        return false;
    }
    const char color = static_cast<char>(std::tolower(static_cast<unsigned char>(piece)));
    return color == (player == Player::Red ? 'r' : 'b');
}

bool Game::isKing(char piece) const {
    return piece == 'R' || piece == 'B';
}

bool Game::isInsideBoard(Position position) const {
    return position.row >= 0 && position.row < BoardSize &&
           position.column >= 0 && position.column < BoardSize;
}

void Game::updateWinner() {
    if (!hasPieces(currentPlayer_) || legalMoves().empty()) {
        winner_ = currentPlayer_ == Player::Red ? Player::Black : Player::Red;
    }
}

}  // namespace checkers
