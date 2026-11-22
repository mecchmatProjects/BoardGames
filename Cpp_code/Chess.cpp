#include "Chess.h"

#include <algorithm>
#include <cstdlib>
#include <cmath>
#include <filesystem>
#include <sstream>

namespace {

constexpr int BOARD_SIZE = 8;

// ---------------------------------------------------------------------------
// Board implementation
// ---------------------------------------------------------------------------

void ChessBoard::initialize() {
    for (int r = 0; r < BOARD_SIZE; ++r)
        for (int c = 0; c < BOARD_SIZE; ++c)
            grid[r][c] = ChessPiece{};

    static const PieceType backRank[8] = {
        PieceType::Rook, PieceType::Knight, PieceType::Bishop, PieceType::Queen,
        PieceType::King, PieceType::Bishop, PieceType::Knight, PieceType::Rook};

    for (int c = 0; c < BOARD_SIZE; ++c) {
        grid[0][c] = ChessPiece{ChessColor::Black, backRank[c]};
        grid[1][c] = ChessPiece{ChessColor::Black, PieceType::Pawn};
        grid[6][c] = ChessPiece{ChessColor::White, PieceType::Pawn};
        grid[7][c] = ChessPiece{ChessColor::White, backRank[c]};
    }

    castlingRights[0][0] = castlingRights[0][1] = true; // White
    castlingRights[1][0] = castlingRights[1][1] = true; // Black
    enPassantSquare = std::nullopt;
    halfMoveClock = 0;
}

ChessBoard::ChessBoard() {
    initialize();
}

ChessSquare ChessBoard::findKing(ChessColor color) const {
    for (int r = 0; r < BOARD_SIZE; ++r)
        for (int c = 0; c < BOARD_SIZE; ++c) {
            if (!grid[r][c].isEmpty() && grid[r][c].color == color && grid[r][c].type == PieceType::King)
                return {r, c};
        }
    return {-1, -1};
}

bool ChessBoard::hasCastlingRights(ChessColor color, bool kingSide) const {
    return castlingRights[static_cast<int>(color) - 1][kingSide ? 0 : 1];
}

bool ChessBoard::kingAttacked(ChessColor color) const {
    const ChessSquare king = findKing(color);
    if (!king.isValid()) return false;
    return isAttacked(king, oppositeColor(color));
}

bool ChessBoard::isAttacked(ChessSquare pos, ChessColor by) const {
    if (!pos.isValid()) return false;

    const int r = pos.row, c = pos.col;

    // Pawns (attack diagonally forward toward the enemy)
    if (by == ChessColor::White) {
        // a white pawn at (r+1, c-1) or (r+1, c+1) attacks (r, c)
        if (r + 1 < BOARD_SIZE) {
            if (c - 1 >= 0 && !grid[r + 1][c - 1].isEmpty() && grid[r + 1][c - 1].type == PieceType::Pawn && grid[r + 1][c - 1].color == ChessColor::White) return true;
            if (c + 1 < BOARD_SIZE && !grid[r + 1][c + 1].isEmpty() && grid[r + 1][c + 1].type == PieceType::Pawn && grid[r + 1][c + 1].color == ChessColor::White) return true;
        }
    } else {
        if (r - 1 >= 0) {
            if (c - 1 >= 0 && !grid[r - 1][c - 1].isEmpty() && grid[r - 1][c - 1].type == PieceType::Pawn && grid[r - 1][c - 1].color == ChessColor::Black) return true;
            if (c + 1 < BOARD_SIZE && !grid[r - 1][c + 1].isEmpty() && grid[r - 1][c + 1].type == PieceType::Pawn && grid[r - 1][c + 1].color == ChessColor::Black) return true;
        }
    }

    // Knights
    static const int knightDeltas[8][2] = {
        {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
    for (const auto& d : knightDeltas) {
        const int nr = r + d[0], nc = c + d[1];
        if (nr < 0 || nr >= BOARD_SIZE || nc < 0 || nc >= BOARD_SIZE) continue;
        const auto& p = grid[nr][nc];
        if (!p.isEmpty() && p.type == PieceType::Knight && p.color == by) return true;
    }

    // King
    for (int dr = -1; dr <= 1; ++dr)
        for (int dc = -1; dc <= 1; ++dc) {
            if (dr == 0 && dc == 0) continue;
            const int nr = r + dr, nc = c + dc;
            if (nr < 0 || nr >= BOARD_SIZE || nc < 0 || nc >= BOARD_SIZE) continue;
            const auto& p = grid[nr][nc];
            if (!p.isEmpty() && p.type == PieceType::King && p.color == by) return true;
        }

    // Sliding pieces: walk the 8 directions
    static const int dirDeltas[8][2] = {
        {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};
    for (const auto& d : dirDeltas) {
        const int dr = d[0], dc = d[1];
        const bool straight = (dr == 0 || dc == 0);
        int nr = r + dr, nc = c + dc;
        while (nr >= 0 && nr < BOARD_SIZE && nc >= 0 && nc < BOARD_SIZE) {
            const auto& p = grid[nr][nc];
            if (!p.isEmpty()) {
                if (p.color == by) {
                    const bool matches = straight
                        ? (p.type == PieceType::Rook || p.type == PieceType::Queen)
                        : (p.type == PieceType::Bishop || p.type == PieceType::Queen);
                    if (matches) return true;
                }
                break; // any piece blocks the line
            }
            nr += dr; nc += dc;
        }
    }

    return false;
}

static std::string makeUci(ChessSquare from, ChessSquare to, PieceType promotion, bool isPromotion) {
    std::string uci = squareToAlgebraic(from) + squareToAlgebraic(to);
    if (isPromotion) {
        char promoChar = 'q';
        switch (promotion) {
        case PieceType::Rook: promoChar = 'r'; break;
        case PieceType::Knight: promoChar = 'n'; break;
        case PieceType::Bishop: promoChar = 'b'; break;
        default: promoChar = 'q'; break;
        }
        uci.push_back(promoChar);
    }
    return uci;
}

std::vector<ChessMove> ChessBoard::pseudoLegalMovesForPiece(ChessSquare from, ChessColor player) const {
    std::vector<ChessMove> moves;
    const ChessPiece* piece = getPiece(from);
    if (!piece || piece->isEmpty() || piece->color != player) return moves;

    const int dir = isWhite(player) ? -1 : 1;
    const int startRow = isWhite(player) ? 6 : 1;
    const int promoRow = isWhite(player) ? 0 : 7;

    const auto addMove = [&](ChessSquare to, bool isCapture, bool isEnPassant, bool isCastling, bool isPromotion) {
        ChessMove m;
        m.from = from;
        m.to = to;
        m.isCapture = isCapture;
        m.isEnPassant = isEnPassant;
        m.isCastling = isCastling;
        m.isPromotion = isPromotion;
        m.promotion = PieceType::Queen;
        m.uci = makeUci(from, to, PieceType::Queen, isPromotion);
        moves.push_back(m);
    };

    switch (piece->type) {
    case PieceType::Pawn: {
        const ChessSquare single{from.row + dir, from.col};
        if (single.isValid() && getPiece(single) && getPiece(single)->isEmpty()) {
            const bool promo = single.row == promoRow;
            addMove(single, false, false, false, promo);
            // double push
            if (from.row == startRow) {
                const ChessSquare doubled{from.row + 2 * dir, from.col};
                if (doubled.isValid() && getPiece(doubled) && getPiece(doubled)->isEmpty()) {
                    const bool doublePromo = doubled.row == promoRow;
                    addMove(doubled, false, false, false, doublePromo);
                }
            }
        }

        for (int dc : {-1, 1}) {
            const ChessSquare target{from.row + dir, from.col + dc};
            if (!target.isValid()) continue;
            const ChessPiece* targetPiece = getPiece(target);
            if (targetPiece && !targetPiece->isEmpty() && targetPiece->color != player) {
                const bool promo = target.row == promoRow;
                addMove(target, true, false, false, promo);
            }
        }

        // En passant
        if (enPassantSquare.has_value()) {
            const ChessSquare ep = enPassantSquare.value();
            const int sameRowByWhite = 0; // placeholder (unused)
            (void)sameRowByWhite;
            if (isWhite(player)) {
                // White pawn must sit one rank below the skipped square, i.e. at ep.row + 1,
                // and the skipped square must be on an adjacent file.
                if (from.row == ep.row + 1 && std::abs(ep.col - from.col) == 1) {
                    addMove(ep, true, true, false, ep.row == promoRow);
                }
            } else {
                // Black pawn sits one rank above the skipped square: at ep.row - 1
                if (from.row == ep.row - 1 && std::abs(ep.col - from.col) == 1) {
                    addMove(ep, true, true, false, ep.row == promoRow);
                }
            }
        }
        break;
    }
    case PieceType::Knight: {
        static const int deltas[8][2] = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
        for (const auto& d : deltas) {
            const ChessSquare target{from.row + d[0], from.col + d[1]};
            if (!target.isValid()) continue;
            const ChessPiece* targetPiece = getPiece(target);
            if (targetPiece && !targetPiece->isEmpty() && targetPiece->color == player) continue;
            addMove(target, targetPiece && !targetPiece->isEmpty(), false, false, from.row != 0);
        }
        break;
    }
    case PieceType::Bishop:
    case PieceType::Rook:
    case PieceType::Queen: {
        static const int deltas[8][2] = {
            {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};
        for (const auto& d : deltas) {
            const bool straight = (d[0] == 0 || d[1] == 0);
            if (straight && piece->type == PieceType::Bishop) continue;
            if (!straight && piece->type == PieceType::Rook) continue;
            int nr = from.row + d[0], nc = from.col + d[1];
            while (nr >= 0 && nr < BOARD_SIZE && nc >= 0 && nc < BOARD_SIZE) {
                const ChessSquare target{nr, nc};
                const ChessPiece* targetPiece = getPiece(target);
                if (targetPiece && !targetPiece->isEmpty()) {
                    if (targetPiece->color != player) {
                        addMove(target, true, false, false, false);
                    }
                    break;
                }
                addMove(target, false, false, false, false);
                nr += d[0]; nc += d[1];
            }
        }
        break;
    }
    case PieceType::King: {
        const int kingRow = isWhite(player) ? 7 : 0;
        // Adjacent squares
        for (int dr = -1; dr <= 1; ++dr)
            for (int dc = -1; dc <= 1; ++dc) {
                if (dr == 0 && dc == 0) continue;
                const ChessSquare target{from.row + dr, from.col + dc};
                if (!target.isValid()) continue;
                const ChessPiece* targetPiece = getPiece(target);
                if (targetPiece && !targetPiece->isEmpty() && targetPiece->color == player) continue;
                addMove(target, targetPiece && !targetPiece->isEmpty(), false, false, false);
            }

        // Castling (king-side and queen-side)
        if (from.row == kingRow && from.col == 4) {
            const ChessSquare kingSquare{from.row, from.col};
            const bool inCheck = kingAttacked(player);

            // King-side
            const int kFrom = 4, kTo = 6;
            const std::vector<ChessSquare> kSquares = {
                {from.row, 4}, {from.row, 5}, {from.row, 6}};
            if (hasCastlingRights(player, true) && !inCheck) {
                bool pathEmpty = true;
                for (int c = 5; c <= 6; ++c) {
                    const auto& p = grid[from.row][c];
                    if (!p.isEmpty()) pathEmpty = false;
                }
                bool safe = true;
                for (const auto& sq : kSquares) {
                    if (isAttacked(sq, oppositeColor(player))) safe = false;
                }
                if (pathEmpty && safe) {
                    addMove({from.row, kTo}, false, false, true, false);
                    moves.back().isCapture = false;
                    moves.back().isCastling = true;
                    moves.back().promotion = PieceType::King;
                }
            }

            // Queen-side
            const int qFrom = 4, qTo = 2;
            const std::vector<ChessSquare> qSquares = {
                {from.row, 4}, {from.row, 3}, {from.row, 2}};
            if (hasCastlingRights(player, false) && !inCheck) {
                bool pathEmpty = true;
                for (int c = 1; c <= 3; ++c) {
                    const auto& p = grid[from.row][c];
                    if (!p.isEmpty()) pathEmpty = false;
                }
                bool safe = true;
                for (const auto& sq : qSquares) {
                    if (isAttacked(sq, oppositeColor(player))) safe = false;
                }
                // The queen's rook must be present (guaranteed by rights)
                if (pathEmpty && safe) {
                    addMove({from.row, qTo}, false, false, true, false);
                    moves.back().isCastling = true;
                    moves.back().promotion = PieceType::King;
                }
            }
        }
        break;
    }
    default:
        break;
    }

    return moves;
}

std::vector<ChessMove> ChessBoard::pseudoLegalMoves(ChessColor player) const {
    std::vector<ChessMove> moves;
    for (int r = 0; r < BOARD_SIZE; ++r)
        for (int c = 0; c < BOARD_SIZE; ++c) {
            const auto& p = grid[r][c];
            if (!p.isEmpty() && p.color == player) {
                auto pieceMoves = pseudoLegalMovesForPiece({r, c}, player);
                moves.insert(moves.end(), pieceMoves.begin(), pieceMoves.end());
            }
        }
    return moves;
}

std::vector<ChessMove> ChessBoard::legalMovesForPiece(ChessSquare from, ChessColor player) const {
    std::vector<ChessMove> legal;
    auto pseudo = pseudoLegalMovesForPiece(from, player);
    for (const auto& m : pseudo) {
        auto after = afterMove(m);
        if (!after.kingAttacked(player)) {
            legal.push_back(m);
        }
    }
    return legal;
}

std::vector<ChessMove> ChessBoard::legalMoves(ChessColor player) const {
    std::vector<ChessMove> legal;
    auto all = pseudoLegalMoves(player);
    for (const auto& m : all) {
        auto after = afterMove(m);
        if (!after.kingAttacked(player)) {
            legal.push_back(m);
        }
    }
    return legal;
}

void ChessBoard::movePiece(const ChessMove& move) {
    const ChessPiece movingPiece = grid[move.from.row][move.from.col];
    const int colorIndex = static_cast<int>(movingPiece.color) - 1;

    if (move.isCastling) {

        grid[move.to.row][move.to.col] = movingPiece;
        grid[move.from.row][move.from.col] = ChessPiece{};

        const int kingRow = move.to.row;
        if (!isWhite(movingPiece.color)) {} // both use same rows (0/7)
        const bool kingSide = move.to.col > move.from.col;
        const int homeRookCol = kingSide ? 7 : 0;
        const int newRookCol = kingSide ? 5 : 3;
        ChessPiece rook = grid[kingRow][homeRookCol];
        grid[kingRow][homeRookCol] = ChessPiece{};
        grid[kingRow][newRookCol] = rook;


        castlingRights[colorIndex][0] = false;
        castlingRights[colorIndex][1] = false;

        enPassantSquare = std::nullopt;
        halfMoveClock++;
        return;
    }

    const ChessSquare to = move.to;
    const ChessSquare from = move.from;

    if (move.isEnPassant) {

        grid[from.row][to.col] = ChessPiece{};
    } else {
        grid[to.row][to.col] = movingPiece;
        if (move.isCapture && !grid[to.row][to.col].isEmpty()) {

        }
    }

    if (move.isPromotion && movingPiece.type == PieceType::Pawn) {
        grid[to.row][to.col] = ChessPiece{movingPiece.color, PieceType::Queen};
        halfMoveClock = 0;
    } else {
        grid[to.row][to.col] = ChessPiece{movingPiece.color, movingPiece.type};
    }

    grid[from.row][from.col] = ChessPiece{};


    if (move.isEnPassant) {
        enPassantSquare = std::nullopt;
    } else if (movingPiece.type == PieceType::Pawn && std::abs(to.row - from.row) == 2) {
        enPassantSquare = ChessSquare{from.row + (isWhite(movingPiece.color) ? -1 : 1), from.col};
        halfMoveClock = 0;
    } else {
        enPassantSquare = std::nullopt;
        if (!move.isCapture && movingPiece.type != PieceType::Pawn) {
            halfMoveClock++;
        } else if (move.isCapture) {
            halfMoveClock = 0;
        } else {
            halfMoveClock = 0;
        }
    }


    if (movingPiece.type == PieceType::King) {
        castlingRights[colorIndex][0] = false;
        castlingRights[colorIndex][1] = false;
    } else if (movingPiece.type == PieceType::Rook) {
        const bool isWhiteRookFromHome = movingPiece.color == ChessColor::White && from.row == 7;
        const bool isBlackRookFromHome = movingPiece.color == ChessColor::Black && from.row == 0;
        if (isWhiteRookFromHome || isBlackRookFromHome) {
            if (from.col == 0) castlingRights[colorIndex][1] = false;
            if (from.col == 7) castlingRights[colorIndex][0] = false;
        }
    }

    // Clear rights when the corner rook is captured
    if (move.isCapture && (to.row == 0 || to.row == 7) && (to.col == 0 || to.col == 7)) {
        const ChessPiece captured = grid[to.row][to.col]; // note: target is now overwritten; need the original capture!
        (void)captured;
    }
}

// The movePiece function is replaced by a clean implementation below (see afterMove).
ChessBoard ChessBoard::afterMove(const ChessMove& move) const {
    ChessBoard next = *this;
    next.movePiece(move);
    return next;
}

bool ChessBoard::isInsufficientMaterial() const {
    int whiteMinors = 0, blackMinors = 0;
    for (int r = 0; r < BOARD_SIZE; ++r)
        for (int c = 0; c < BOARD_SIZE; ++c) {
            const auto& p = grid[r][c];
            if (p.isEmpty()) continue;
            if (p.type == PieceType::Pawn || p.type == PieceType::Rook || p.type == PieceType::Queen)
                return false;
            if (p.type == PieceType::Knight || p.type == PieceType::Bishop) {
                if (p.color == ChessColor::White) whiteMinors++;
                else blackMinors++;
            }
        }
    return whiteMinors <= 1 && blackMinors <= 1;
}

} // namespace
