#include "Chess.h"

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <sstream>
#include <cstdlib>

#ifndef _WIN32
#include <sys/wait.h>
#include <csignal>
#include <cerrno>
#include <signal.h>
#else
#include <process.h>
#endif

namespace {
constexpr int BOARD_SIZE = 8;

#ifdef _WIN32
#else
// The header stores POSIX pipe file descriptors as void*.
inline void* fdToPtr(int fd) { return reinterpret_cast<void*>(static_cast<std::uintptr_t>(fd)); }
inline int ptrToFd(void* p) { return static_cast<int>(reinterpret_cast<std::uintptr_t>(p)); }
#endif
}

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
        for (int c = 0; c < BOARD_SIZE; ++c)
            if (!grid[r][c].isEmpty() && grid[r][c].color == color && grid[r][c].type == PieceType::King)
                return ChessSquare{r, c};
    return ChessSquare{-1, -1};
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

    // An attacking pawn sits one row "behind" the target, on a diagonal
    if (by == ChessColor::White) {
        if (r + 1 < BOARD_SIZE) {
            if (c > 0 && !grid[r + 1][c - 1].isEmpty() &&
                grid[r + 1][c - 1].type == PieceType::Pawn && grid[r + 1][c - 1].color == ChessColor::White)
                return true;
            if (c + 1 < BOARD_SIZE && !grid[r + 1][c + 1].isEmpty() &&
                grid[r + 1][c + 1].type == PieceType::Pawn && grid[r + 1][c + 1].color == ChessColor::White)
                return true;
        }
    } else {
        if (r > 0) {
            if (c > 0 && !grid[r - 1][c - 1].isEmpty() &&
                grid[r - 1][c - 1].type == PieceType::Pawn && grid[r - 1][c - 1].color == ChessColor::Black)
                return true;
            if (c + 1 < BOARD_SIZE && !grid[r - 1][c + 1].isEmpty() &&
                grid[r - 1][c + 1].type == PieceType::Pawn && grid[r - 1][c + 1].color == ChessColor::Black)
                return true;
        }
    }

    static const int knightDeltas[8][2] = {
        {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
    for (const auto& d : knightDeltas) {
        const int nr = r + d[0], nc = c + d[1];
        if (nr < 0 || nr >= BOARD_SIZE || nc < 0 || nc >= BOARD_SIZE) continue;
        const ChessPiece& p = grid[nr][nc];
        if (!p.isEmpty() && p.type == PieceType::Knight && p.color == by) return true;
    }

    for (int dr = -1; dr <= 1; ++dr)
        for (int dc = -1; dc <= 1; ++dc) {
            if (dr == 0 && dc == 0) continue;
            const int nr = r + dr, nc = c + dc;
            if (nr < 0 || nr >= BOARD_SIZE || nc < 0 || nc >= BOARD_SIZE) continue;
            const ChessPiece& p = grid[nr][nc];
            if (!p.isEmpty() && p.type == PieceType::King && p.color == by) return true;
        }

    static const int dirDeltas[8][2] = {
        {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};
    for (const auto& d : dirDeltas) {
        int nr = r + d[0], nc = c + d[1];
        while (nr >= 0 && nr < BOARD_SIZE && nc >= 0 && nc < BOARD_SIZE) {
            const ChessPiece& p = grid[nr][nc];
            if (!p.isEmpty()) {
                if (p.color == by &&
                    (d[0] == 0 || d[1] ==0 ? (p.type == PieceType::Rook || p.type == PieceType::Queen)
                                           : (p.type == PieceType::Bishop || p.type == PieceType::Queen)))
                    return true;
                break;
            }
            nr += d[0];
            nc += d[1];
        }
    }

    return false;
}

static std::string makeUci(ChessSquare from, ChessSquare to, PieceType promotion) {
    std::string uci = squareToAlgebraic(from) + squareToAlgebraic(to);
    switch (promotion) {
    case PieceType::Rook: uci += 'r'; break;
    case PieceType::Knight: uci += 'n'; break;
    case PieceType::Bishop: uci += 'b'; break;
    default: uci += 'q'; break;
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

    const auto addMove = [&](ChessSquare to, bool isCapture, bool isEnPassant,
                             bool isCastling, PieceType promotion) {
        ChessMove m;
        m.from = from;
        m.to = to;
        m.isCapture = isCapture;
        m.isEnPassant = isEnPassant;
        m.isCastling = isCastling;
        m.isPromotion = (promotion != PieceType::Pawn);
        m.promotion = (promotion == PieceType::Pawn ? PieceType::Queen : promotion);
        m.uci = makeUci(from, to, m.promotion) ;
        if (!m.isPromotion) m.uci = squareToAlgebraic(from) + squareToAlgebraic(to);
        moves.push_back(m);
    };

    switch (piece->type) {
    case PieceType::Pawn: {
        const ChessSquare single{from.row + dir, from.col};
        if (single.isValid() && getPiece(single)->isEmpty()) {
            const bool promo = (single.row == promoRow);
            addMove(single, false, false, false, promo ? PieceType::Queen : PieceType::Pawn);
            if (from.row == startRow) {
                const ChessSquare doubled{from.row + 2 * dir, from.col};
                if (doubled.isValid() && getPiece(doubled)->isEmpty()) {
                    // The intermediate square is already known empty; both push types land empty.
                    addMove(doubled, false, false, false,
                            (doubled.row == promoRow) ? PieceType::Queen : PieceType::Pawn);
                }
            }
        }

        for (int dc : {-1, 1}) {
            const ChessSquare target{from.row + dir, from.col + dc};
            if (!target.isValid()) continue;
            const ChessPiece* victim = getPiece(target);
            if (victim && !victim->isEmpty() && victim->color != player) {
                addMove(target, true, false, false,
                        (target.row == promoRow) ? PieceType::Queen : PieceType::Pawn);
            } else if (!victim || victim->isEmpty()) {
                // En passant capture on the vacated square
                if (enPassantSquare.has_value() &&
                    enPassantSquare->row == target.row && enPassantSquare->col == target.col) {
                    addMove(target, true, true, false, PieceType::Pawn);
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
            const ChessPiece& occ = grid[target.row][target.col];
            if (!occ.isEmpty() && occ.color == player) continue;
            addMove(target, !occ.isEmpty(), false, false, PieceType::Pawn);
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
                const ChessPiece& occ = grid[nr][nc];
                if (!occ.isEmpty()) {
                    if (occ.color != player) addMove(target, true, false, false, PieceType::Pawn);
                    break;
                }
                addMove(target, false, false, false, PieceType::Pawn);
                nr += d[0];
                nc += d[1];
            }
        }
        break;
    }
    case PieceType::King: {
        for (int dr = -1; dr <= 1; ++dr)
            for (int dc = -1; dc <= 1; ++dc) {
                if (dr == 0 && dc == 0) continue;
                const ChessSquare target{from.row + dr, from.col + dc};
                if (!target.isValid()) continue;
                const ChessPiece& occ = grid[target.row][target.col];
                if (!occ.isEmpty() && occ.color == player) continue;
                addMove(target, !occ.isEmpty(), false, false, PieceType::Pawn);
            }

        // Castling. Rights guarantee the pieces never moved since game start,
        // so we only verify empty paths and safe squares here.
        if (from.row == (isWhite(player) ? 7 : 0) && from.col == 4) {
            const ChessColor opp = oppositeColor(player);
            if (!kingAttacked(player)) {
                if (hasCastlingRights(player, true)) {
                    const ChessSquare dest{from.row, 6};
                    if (grid[from.row][5].isEmpty() && grid[from.row][6].isEmpty() &&
                        !isAttacked(dest, opp))
                        addMove(dest, false, false, true, PieceType::Pawn);
                }
                if (hasCastlingRights(player, false)) {
                    const ChessSquare dest{from.row, 2};
                    if (grid[from.row][3].isEmpty() && grid[from.row][2].isEmpty() &&
                        grid[from.row][1].type != PieceType::King && // the home rook must still be here
                        !grid[from.row][1].isEmpty() && grid[from.row][1].color == player &&
                        !isAttacked(dest, opp))
                        addMove(dest, false, false, true, PieceType::Pawn);
                }
            }
        }
        break;
    }
    }

    return moves;
}

std::vector<ChessMove> ChessBoard::pseudoLegalMoves(ChessColor player) const {
    std::vector<ChessMove> moves;
    for (int r = 0; r < BOARD_SIZE; ++r)
        for (int c = 0; c < BOARD_SIZE; ++c)
            if (!grid[r][c].isEmpty() && grid[r][c].color == player) {
                auto pieceMoves = pseudoLegalMovesForPiece(ChessSquare{r, c}, player);
                moves.insert(moves.end(), pieceMoves.begin(), pieceMoves.end());
            }
    return moves;
}

std::vector<ChessMove> ChessBoard::legalMovesForPiece(ChessSquare from, ChessColor player) const {
    std::vector<ChessMove> legal;
    for (const auto& m : pseudoLegalMovesForPiece(from, player)) {
        ChessBoard after = afterMove(m);
        if (!after.kingAttacked(player)) legal.push_back(m);
    }
    return legal;
}

std::vector<ChessMove> ChessBoard::legalMoves(ChessColor player) const {
    std::vector<ChessMove> legal;
    for (const auto& m : pseudoLegalMoves(player)) {
        ChessBoard after = afterMove(m);
        if (!after.kingAttacked(player)) legal.push_back(m);
    }
    return legal;
}

void ChessBoard::movePiece(const ChessMove& move) {
    if (!move.from.isValid() || !move.to.isValid()) return;

    const int fr = move.from.row, fc = move.from.col;
    const int tr = move.to.row, tc = move.to.col;
    const ChessPiece moving = grid[fr][fc];
    const int ci = static_cast<int>(moving.color) - 1;

    // Clear the appropriate rights when a home-corner rook is captured
    // (a piece that roamed into a corner only matters when rights are still set).
    const auto clearHomeRights = [&](int row, int col) {
        if ((row != 0 && row != 7) || (col != 0 && col != 7)) return;
        const int sideIdx = (row == 0) ? 1 : 0; // row 0 = black home, row 7 = white home
        if (col == 0) castlingRights[sideIdx][1] = false;
        else castlingRights[sideIdx][0] = false;
    };

    if (move.isCapture && !move.isEnPassant) clearHomeRights(tr, tc);

    if (move.isCastling) {
        const bool kingSide = (tc > fc);
        grid[tr][tc] = moving;
        grid[fr][fc] = ChessPiece{};
        if (kingSide) {
            grid[tr][5] = grid[tr][7];
            grid[tr][7] = ChessPiece{};
        } else {
            grid[tr][3] = grid[tr][1];
            grid[tr][1] = ChessPiece{};
        }
        castlingRights[ci][0] = castlingRights[ci][1] = false;
        enPassantSquare = std::nullopt;
        ++halfMoveClock; // king move, not a capture
        return;
    }

    if (move.isEnPassant) grid[fr][tc] = ChessPiece{}; // remove the captured pawn

    ChessPiece placed = moving;
    if (move.isPromotion) placed = ChessPiece{moving.color, move.promotion};
    grid[tr][tc] = placed;
    grid[fr][fc] = ChessPiece{};

    if (moving.type == PieceType::Pawn && std::abs(tr - fr) == 2 && !move.isCapture)
        enPassantSquare = ChessSquare{fr + (isWhite(moving.color) ? -1 : 1), fc};
    else
        enPassantSquare = std::nullopt;

    if (move.isCapture || moving.type == PieceType::Pawn) halfMoveClock = 0;
    else ++halfMoveClock;

    if (moving.type == PieceType::King) {
        castlingRights[ci][0] = false;
        castlingRights[ci][1] = false;
    } else if (moving.type == PieceType::Rook) {
        if ((fr == 0 || fr == 7) && (fc == 0 || fc == 7)) clearHomeRights(fr, fc);
    }
}

ChessBoard ChessBoard::afterMove(const ChessMove& move) const {
    ChessBoard next = *this;
    next.movePiece(move);
    return next;
}

bool ChessBoard::isInsufficientMaterial() const {
    int whiteMinors = 0, blackMinors = 0;
    for (int r = 0; r < BOARD_SIZE; ++r)
        for (int c = 0; c < BOARD_SIZE; ++c) {
            const ChessPiece& p = grid[r][c];
            if (p.isEmpty()) continue;
            if (p.type == PieceType::Pawn || p.type == PieceType::Rook || p.type == PieceType::Queen)
                return false;
            if (p.type == PieceType::Knight || p.type == PieceType::Bishop) {
                if (p.color == ChessColor::White) ++whiteMinors;
                else ++blackMinors;
            }
        }
    return whiteMinors <= 1 && blackMinors <= 1;
}

// ---------------------------------------------------------------------------
// StockfishEngine implementation (UCI over pipes; search runs on a worker)
// ---------------------------------------------------------------------------

StockfishEngine::StockfishEngine(std::string path) : exePath(std::move(path)) {
#ifndef _WIN32
    std::signal(SIGPIPE, SIG_IGN); // writing into a pipe of a dead engine must not crash us
#endif
}

StockfishEngine::~StockfishEngine() {
    cancel();
    shutdown();
}

bool StockfishEngine::spawnProcess(const std::string&) {
#ifdef _WIN32
    HANDLE hReadChildStdin = nullptr, hWriteParentStdin = nullptr;
    HANDLE hReadParentStdout = nullptr, hWriteChildStdout = nullptr;
    if (!CreatePipe(&hReadChildStdin, &hWriteParentStdin, nullptr, 0)) return false;
    if (!CreatePipe(&hReadParentStdout, &hWriteChildStdout, nullptr, 0)) {
        CloseHandle(hReadChildStdin); CloseHandle(hWriteParentStdin);
        return false;
    }

    PROCESS_INFORMATION pi{};
    if (!CreateProcessA(nullptr, const_cast<char*>(exePath.data()), nullptr,
                        hReadChildStdin, nullptr, hWriteChildStdout,
                        nullptr, nullptr, nullptr, &pi)) {
        CloseHandle(hReadChildStdin);
        CloseHandle(hWriteParentStdin);
        CloseHandle(hReadParentStdout);
        CloseHandle(hWriteChildStdout);
        return false;
    }
    CloseHandle(hReadChildStdin); // child's end is owned by the process
    CloseHandle(hWriteChildStdout);

    processHandle = pi.hProcess;
    threadHandle = pi.hThread;
    stdinWrite = hWriteParentStdin;
    stdoutRead = hReadParentStdout;
    return true;
#else
    int inPipe[2], outPipe[2];
    if (pipe(inPipe) != 0) return false;
    if (pipe(outPipe) != 0) { close(inPipe[0]); close(inPipe[1]); return false; }

    const pid_t pid = fork();
    if (pid < 0) {
        close(inPipe[0]); close(inPipe[1]);
        close(outPipe[0]); close(outPipe[1]);
        return false;
    }
    if (pid == 0) {
        // Child: read stdin comes from parent, write stdout goes to parent.
        dup2(inPipe[0], 0);
        dup2(outPipe[1], 1);
        const std::string& engine = exePath; // execvp searches PATH when there is no '/'
        char* exe = const_cast<char*>(engine.c_str());
        char* args[] = { exe, nullptr };
        execvp(exe, args);
        _exit(127); // only reached when exec failed
    }

    processHandle = pid;
    stdinWrite = fdToPtr(inPipe[1]);
    stdoutRead = fdToPtr(outPipe[0]);
    close(inPipe[0]);
    close(outPipe[1]);
    return true;
#endif
}

void StockfishEngine::requestMove(const std::string& positionArg, int movetimeMs) {
    cancel(); // fully release any previous search/session
    if (exePath.empty()) {
        error = "Engine path not configured.";
        status.store(EngineStatus::Error);
        return;
    }
    std::string pos = positionArg.empty() ? std::string("position startpos") : positionArg;
    if (!spawnProcess(pos)) {
        error = "Failed to start engine process.";
        status.store(EngineStatus::Error);
        bestMove.clear();
        return;
    }
    status.store(EngineStatus::Thinking);
    if (worker.joinable()) worker.join(); // defensive: cancel() already joined
    worker = std::thread(&StockfishEngine::runWorker, this, pos, movetimeMs);
}

EngineStatus StockfishEngine::poll(std::string& bestMoveOut, std::string& errorOut) {
    bestMoveOut.clear();
    errorOut.clear();
    const EngineStatus st = status.load();
    std::lock_guard lock(mutex);
    if (st == EngineStatus::Done) bestMoveOut = bestMove;
    else if (st == EngineStatus::Error) errorOut = error;
    return st;
}

void StockfishEngine::cancel() {
    {
        std::lock_guard lock(mutex);
        status.store(EngineStatus::Idle);
#ifdef _WIN32
        if (stdinWrite != nullptr) { CloseHandle(stdinWrite); stdinWrite = nullptr; }
        if (stdoutRead != nullptr) { CloseHandle(stdoutRead); stdoutRead = nullptr; }
        if (processHandle != nullptr) { TerminateProcess(processHandle, 1); processHandle = nullptr; }
#else
        if (stdinWrite != nullptr) { close(ptrToFd(stdinWrite)); stdinWrite = nullptr; }
        if (processHandle != -1) { kill(processHandle, SIGKILL); processHandle = -1; }
#endif
    }
    if (worker.joinable()) worker.join(); // unblocks via EOF after the process died
}

void StockfishEngine::setExePath(const std::string& path) {
    cancel();
    exePath = path;
}

void StockfishEngine::shutdown() {
#ifdef _WIN32
    std::lock_guard lock(mutex);
    if (stdinWrite != nullptr) { CloseHandle(stdinWrite); stdinWrite = nullptr; }
    if (stdoutRead != nullptr) { CloseHandle(stdoutRead); stdoutRead = nullptr; }
#else
    std::lock_guard lock(mutex);
    if (stdinWrite != nullptr) { int fd = ptrToFd(stdinWrite); close(fd); stdinWrite = nullptr; }
    if (stdoutRead != nullptr) { int fd = ptrToFd(stdoutRead); close(fd); stdoutRead = nullptr; }
#endif
}

void StockfishEngine::runWorker(std::string positionArg, int movetimeMs) {
#ifdef _WIN32
    HANDLE in = stdinWrite;
    HANDLE out = stdoutRead;

    const std::string cmd = "ucinewgame\n" + positionArg + "\ngo movetime " + std::to_string(movetimeMs) + "\nquit\n";
    if (in != nullptr) {
        WriteProcessPipe(in, WRITE_PIPE_SILENCE_FAILURES, const_cast<char*>(cmd.data()), static_cast<DWORD>(cmd.size()));
        CloseHandle(in);
        stdinWrite = nullptr;
    }

    std::string line, best;
    bool gotBest = false;
    char ch;
    while (out != nullptr && ReadProcessPipe(out, &ch, 1) == 1) {
        if (ch == '\n') {
            if (line.rfind("bestmove ", 0) == 0) {
                std::istringstream ss(line); std::string kw, mv;
                ss >> kw >> mv;
                if (!mv.empty()) best = mv; // may be "(none)"
                gotBest = !best.empty();
                break;
            }
            line.clear();
        } else if (ch != '\r') {
            line.push_back(ch);
        }
        if (line.size() > 4096) line.clear();
    }
    if (out != nullptr) { CloseHandle(out); stdoutRead = nullptr; }

    {
        std::lock_guard lock(mutex);
        if (status.load() != EngineStatus::Thinking) return; // cancelled meanwhile
        if (gotBest && best != "(none)") {
            bestMove = best; status.store(EngineStatus::Done);
        } else {
            status.store(EngineStatus::Error);
            error = "Stockfish did not return a move.";
        }
    }
#else
    const int inFd = (stdinWrite != nullptr) ? ptrToFd(stdinWrite) : -1;
    const int outFd = (stdoutRead != nullptr) ? ptrToFd(stdoutRead) : -1;

    const std::string cmd = "ucinewgame\n" + positionArg + "\ngo movetime " + std::to_string(movetimeMs) + "\nquit\n";
    size_t off = 0;
    while (inFd >= 0 && off < cmd.size()) {
        const ssize_t w = write(inFd, cmd.data() + off, cmd.size() - off);
        if (w > 0) off += static_cast<size_t>(w);
        else if (w < 0 && errno == EINTR) continue;
        else break;
    }
    if (inFd >= 0) close(inFd);
    {
        std::lock_guard lock(mutex);
        stdinWrite = nullptr;
    }

    std::string line, best;
    bool gotBest = false;
    bool sawNone = false;
    char ch;
    while (outFd >= 0 && read(outFd, &ch, 1) == 1) {
        if (ch == '\n') {
            if (line.rfind("bestmove ", 0) == 0) {
                std::istringstream ss(line);
                std::string kw, mv;
                ss >> kw >> mv;
                if (!mv.empty()) {
                    if (mv == "(none)") sawNone = true;
                    else best = mv;
                }
                gotBest = !best.empty() || sawNone;
                break;
            }
            line.clear();
        } else if (ch != '\r') {
            if (line.size() < 4096) line.push_back(ch);
        }
    }
    if (outFd >= 0) close(outFd);
    {
        std::lock_guard lock(mutex);
        stdoutRead = nullptr;
    }

    int exitCode = -1;
    if (processHandle != -1) {
        const pid_t pid = processHandle;
        int wstatus = 0;
        while (waitpid(pid, &wstatus) < 0 && errno == EINTR) {}
        if (WIFEXITED(wstatus)) exitCode = WEXITSTATUS(wstatus);
        else exitCode = -42; // e.g. killed by cancel(); status then reset to Idle
    }

    {
        std::lock_guard lock(mutex);
        if (status.load() != EngineStatus::Thinking) return; // cancelled meanwhile
        if (gotBest && !best.empty()) {
            bestMove = best;
            status.store(EngineStatus::Done);
        } else if (sawNone) {
            status.store(EngineStatus::Error);
            error = "Engine has no move; position should be finished.";
        } else if (exitCode == 127) {
        status.store(EngineStatus::Error);
            error = "Stockfish binary not found: '" + exePath + "'. Press 'F' to set its path.";
        } else {
            status.store(EngineStatus::Error);
            error = "Stockfish died without a move (exit code " + std::to_string(exitCode) + ").";
        }
    }
#endif
}

// ---------------------------------------------------------------------------
// ChessGame implementation
// ---------------------------------------------------------------------------

static std::string colorName(ChessColor c) {
    switch (c) {
    case ChessColor::White: return "White";
    case ChessColor::Black: return "Black";
    default: return "None";
    }
}


ChessGame::ChessGame()
    : window(sf::VideoMode(static_cast<unsigned>(BOARD_PIXELS),
                           static_cast<unsigned>(BOARD_PIXELS + STATUS_HEIGHT)),
             "Chess - White's Turn (Press R to restart)",
             sf::Style::Titlebar | sf::Style::Close),
      engine("stockfish"),
      currentTurn(ChessColor::White),
      isGameOver(false) {
    window.setFramerateLimit(60);

    const char* fontCandidates[] = {
        "DejaVuSans.ttf",
        "/usr/share/fonts/dejavu-sans-fonts/DejaVuSans.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"};
    for (const char* f : fontCandidates) {
        if (font.openFromFile(f)) { fontLoaded = true; break; }
    }

    updateWindowTitle();
}

void ChessGame::run() {
    while (window.isOpen()) {
        processEvents();
        pollEngine();
        render();
    }
}

void ChessGame::processEvents() {
    sf::Event event;
    while (window.pollEvent(event)) {
        if (event.type == sf::Event::Closed) {
            window.close();
        } else if (event.type == sf::Event::KeyPressed) {
            if (event.key.code == sf::Keyboard::R) {
                resetGame();
            } else if (event.key.code == sf::Keyboard::B) {
                botMode = !botMode;
                engineError.clear();
                if (engineThinking) { engine.cancel(); engineThinking = false; }
                if (botMode) startEngineIfNeeded();
                updateWindowTitle();
            }
        } else if (event.type == sf::Event::Text) {
            const sf::Uint32 t = event.text;
            if (!enginePathInput) {
                if (t == 'f' || t == 'F') { enginePathInput = true; enginePathBuffer.clear(); }
            } else if (t == '\r' || t == '\n') {
                if (!enginePathBuffer.empty()) {
                    engine.setExePath(enginePathBuffer);
                    engineError.clear();
                }
                enginePathInput = false;
                updateWindowTitle();
            } else if (t >= 32 && t < 127 && enginePathBuffer.size() < 160) {
                enginePathBuffer.push_back(static_cast<char>(t));
            }
        } else if (event.type == sf::Event::MouseButtonPressed && !isGameOver) {
            if (event.mouseButton.button == sf::Mouse::Left)
                handleMouseClick(event.mouseButton.x, event.mouseButton.y);
        }
    }
}

void ChessGame::handleMouseClick(int x, int y) {
    if (y >= BOARD_PIXELS) return; // status bar area

    const ChessSquare clicked{static_cast<int>(y / TILE_SIZE), static_cast<int>(x / TILE_SIZE)};
    if (!clicked.isValid()) return;

    // While it is the bot's turn, the human may only watch.
    if (botMode && !isWhite(currentTurn)) return;

    if (selectedPos.has_value()) {
        for (const auto& m : highlightedMoves) {
            if (m.to == clicked) {
                applyChosenMove(m);
                startEngineIfNeeded();
                updateWindowTitle();
                return;
            }
        }
    }

    const ChessPiece* clickedPiece = board.getPiece(clicked);
    if (clickedPiece && !clickedPiece->isEmpty() && clickedPiece->color == currentTurn) {
        selectedPos = clicked;
        refreshHighlightedMoves();
    } else {
        selectedPos.reset();
        highlightedMoves.clear();
    }
}

void ChessGame::refreshHighlightedMoves() {
    highlightedMoves.clear();
    if (selectedPos.has_value())
        highlightedMoves = board.legalMovesForPiece(selectedPos.value(), currentTurn);
}

void ChessGame::applyChosenMove(const ChessMove& move) {
    moveHistory.push_back(move.uci);
    board = board.afterMove(move);
    lastMove = move;
    selectedPos.reset();
    highlightedMoves.clear();
    currentTurn = oppositeColor(currentTurn);
    checkGameOver();
}

void ChessGame::startEngineIfNeeded() {
    if (!botMode || engineThinking || isGameOver) return;
    if (!engineError.empty()) return;
    if (currentTurn != ChessColor::Black) return; // the bot always plays Black
    engine.requestMove(buildPositionArgument(), 1000);
    engineThinking = true;
}

void ChessGame::pollEngine() {
    if (!engineThinking) return;
    std::string best, err;
    const EngineStatus st = engine.poll(best, err);
    if (st == EngineStatus::Idle) {
        engineThinking = false;
        startEngineIfNeeded(); // e.g. after cancel triggered by setExePath
    } else if (st == EngineStatus::Done) {
        engineThinking = false;
        applyEngineMove(best);
    } else if (st == EngineStatus::Error) {
        engineThinking = false;
        engineError = err;
        updateWindowTitle();
    }
}

void ChessGame::applyEngineMove(const std::string& bestMove) {
    auto legal = board.legalMoves(currentTurn);
    const ChessMove* found = nullptr;
    for (const auto& m : legal)
        if (m.uci == bestMove) { found = &m; break; }
    if (!found) {
        for (const auto& m : legal) {
            if (m.uci.size() > 4 && bestMove.size() == 4 && m.uci.substr(0, 4) == bestMove) {
                found = &m;
                break;
            }
        }
    }
    if (found) {
        applyChosenMove(*found);
        updateWindowTitle();
    } else {
        engineError = "Engine returned an illegal move: '" + bestMove + "'. Press 'B' to retry.";
        updateWindowTitle();
    }
}

void ChessGame::render() {
    window.clear();
    drawBoard();
    drawMoveHighlights();
    drawLastMoveHighlight();
    drawStatusBar(currentStatusText());
    window.display();
}

void ChessGame::drawBoard() {
    const sf::Color light(238, 238, 210);
    const sf::Color dark(118, 150, 86);
    const sf::Color sel(196, 212, 88);

    sf::RectangleShape tile(sf::Vector2f(TILE_SIZE, TILE_SIZE));
    for (int r = 0; r < BOARD_SIZE; ++r) {
        for (int c = 0; c < BOARD_SIZE; ++c) {
            tile.setPosition(static_cast<float>(c) * TILE_SIZE, static_cast<float>(r) * TILE_SIZE);
            if (selectedPos.has_value() && selectedPos->row == r && selectedPos->col == c)
                tile.setFillColor(sel);
            else if ((r + c) % 2 == 0)
                tile.setFillColor(light);
            else
                tile.setFillColor(dark);
            window.draw(tile);
        }
    }

    for (int r = 0; r < BOARD_SIZE; ++r)
        for (int c = 0; c < BOARD_SIZE; ++c) {
            const ChessPiece* piece = board.getPiece(ChessSquare{r, c});
            if (piece && !piece->isEmpty())
                drawPiece(ChessSquare{r, c}, *piece);
        }
}

void ChessGame::checkGameOver() {
    gameOverReason.clear();
    isGameOver = false;

    const auto lm = board.legalMoves(currentTurn);
    if (lm.empty()) {
        isGameOver = true;
        if (board.isInCheck(currentTurn)) {
            const ChessColor winner = oppositeColor(currentTurn);
            gameOverReason = std::string("Checkmate - ") + colorName(winner) + " wins!";
        } else {
            gameOverReason = "Draw - stalemate.";
        }
    } else if (board.isFiftyMoveDraw()) {
        isGameOver = true;
        gameOverReason = "Draw - fifty-move rule.";
    } else if (board.isInsufficientMaterial()) {
        isGameOver = true;
        gameOverReason = "Draw - insufficient material.";
    }
}

void ChessGame::drawStatusBar(const std::string& statusText) {
    const float barY = static_cast<float>(BOARD_PIXELS);

    sf::RectangleShape bar(sf::Vector2f(static_cast<float>(BOARD_PIXELS), STATUS_HEIGHT));
    bar.setPosition(0.f, barY);
    bar.setFillColor(sf::Color(32, 32, 48));
    window.draw(bar);

    if (!fontLoaded) return; // window title carries the same info

    const std::string line1 = !engineError.empty() ? engineError : statusText;
    const sf::Color line1Color = !engineError.empty() ? sf::Color(240, 90, 80) : sf::Color(240, 240, 225);

    sf::Text text;
    text.setFont(font);
    text.setCharacterSize(16);
    text.setString(line1);
    text.setFillColor(line1Color);
    text.setPosition(8.f, barY + 4.f);
    window.draw(text);

    if (!engineError.empty()) {
        text.setCharacterSize(13);
        text.setString("B retry search  F set engine path");
        text.setFillColor(sf::Color(150, 150, 170));
        text.setPosition(8.f, barY + 28.f);
        window.draw(text);
    } else {
        text.setCharacterSize(13);
        text.setString(std::string("B: play vs Stockfish (Bot = Black)   ") +
                       "R: restart   F: set engine path");
        text.setFillColor(sf::Color(150, 150, 170));
        text.setPosition(8.f, barY + 34.f);
        window.draw(text);
    }
}

void ChessGame::drawMoveHighlights() {
    for (const auto& m : highlightedMoves) {
        const float cx = static_cast<float>(m.to.col) * TILE_SIZE + TILE_SIZE / 2.f;
        const float cy = static_cast<float>(m.to.row) * TILE_SIZE + TILE_SIZE / 2.f;
        const float radius = TILE_SIZE * 0.17f;

        sf::CircleShape marker(radius);
        marker.setOrigin(radius, radius);
        marker.setPosition(cx, cy);

        if (m.isPromotion) {
            marker.setFillColor(sf::Color(255, 205, 60, 220));
        } else if (m.isCapture) {
            marker.setFillColor(sf::Color(225, 60, 55, 210));
        } else {
            marker.setFillColor(sf::Color(40, 130, 230, 185));
        }
        marker.setOutlineColor(sf::Color(255, 255, 255, 200));
        marker.setOutlineThickness(2.f);
        window.draw(marker);
    }
}

void ChessGame::drawLastMoveHighlight() {
    if (!lastMove.has_value()) return;

    sf::RectangleShape mark(sf::Vector2f(TILE_SIZE - 8.f, TILE_SIZE - 8.f));
    mark.setFillColor(sf::Color::Transparent);
    mark.setOutlineColor(sf::Color(255, 205, 60));
    mark.setOutlineThickness(3.f);

    mark.setPosition(static_cast<float>(lastMove->from.col) * TILE_SIZE + 4.f,
                     static_cast<float>(lastMove->from.row) * TILE_SIZE + 4.f);
    window.draw(mark);
    mark.setPosition(static_cast<float>(lastMove->to.col) * TILE_SIZE + 4.f,
                     static_cast<float>(lastMove->to.row) * TILE_SIZE + 4.f);
    window.draw(mark);
}

void ChessGame::drawPiece(const ChessSquare& square, const ChessPiece& piece) {
    const float cx = static_cast<float>(square.col) * TILE_SIZE + TILE_SIZE / 2.f;
    const float cy = static_cast<float>(square.row) * TILE_SIZE + TILE_SIZE / 2.f;
    const bool whiteSide = isWhite(piece.color);

    if (fontLoaded) {
        static const sf::Uint32 glyphs[6] = {
            0x265F, // pawn
            0x265C, // rook
            0x265D, // knight
            0x265E, // bishop
            0x265B, // queen
            0x265A  // king
        };

        sf::Text text;
        text.setFont(font);
        text.setCharacterSize(54);
        const sf::Uint32 glyph = glyphs[static_cast<int>(piece.type)];
        text.setString(sf::String(glyph));

        if (whiteSide) {
            text.setFillColor(sf::Color(248, 245, 238));
            text.setOutlineColor(sf::Color(30, 30, 30));
            text.setOutlineThickness(1.2f);
        } else {
            text.setFillColor(sf::Color(35, 30, 28));
            text.setOutlineColor(sf::Color(160, 155, 140));
            text.setOutlineThickness(0.8f);
        }

        const sf::FloatRect b = text.getLocalBounds();
        text.setPosition(cx - b.left - b.width / 2.f,
                         cy - b.top - b.height / 2.f);
        window.draw(text);
        return;
    }

    // Fallback without a usable font: chess men as flat discs (like Checkers),
    // kings get a gold ring like promoted checkers pieces.
    float radius = TILE_SIZE * (piece.type == PieceType::King ? 0.42f : 0.40f);
    if (piece.type == PieceType::Pawn) radius = TILE_SIZE * 0.32f;

    sf::CircleShape disc(radius);
    disc.setOrigin(radius, radius);
    disc.setPosition(cx, cy);
    disc.setFillColor(whiteSide ? sf::Color(245, 242, 235) : sf::Color(35, 30, 28));
    disc.setOutlineColor(whiteSide ? sf::Color(70, 70, 70) : sf::Color(30, 30, 30));
    disc.setOutlineThickness(whiteSide ? 2.5f : 2.f);
    if (piece.type == PieceType::King) {
        disc.setOutlineColor(sf::Color(255, 215, 0));
        disc.setOutlineThickness(4.f);
    }
    window.draw(disc);
}

std::string ChessGame::buildPositionArgument() const {
    std::string s = "position startpos";
    if (!moveHistory.empty()) {
        s += " moves";
        for (const auto& m : moveHistory) {
            s += " ";
            s += m;
        }
    }
    return s;
}

std::string ChessGame::currentStatusText() const {
    if (isGameOver) return gameOverReason + " (press R to restart)";

    std::string s = colorName(currentTurn) + "'s turn";
    if (botMode && currentTurn == ChessColor::Black) {
        s += " (Stockfish";
        if (engineThinking) s += " is thinking";
        s += ")";
    } else {
        s += " (click a piece, then a highlighted square)";
    }
    if (board.isInCheck(currentTurn)) s += " - CHECK!";
    return s;
}

void ChessGame::updateWindowTitle() {
    std::string title = "Chess - " + currentStatusText();
    if (botMode) title += " | Bot: Black";
    if (!engineError.empty()) title += " | " + engineError;
    window.setTitle(title);
}

void ChessGame::resetGame() {
    board = ChessBoard();
    moveHistory.clear();
    lastMove.reset();
    selectedPos.reset();
    highlightedMoves.clear();
    currentTurn = ChessColor::White;
    isGameOver = false;
    gameOverReason.clear();
    if (engineThinking) { engine.cancel(); engineThinking = false; }
    engineError.clear();
    updateWindowTitle();
}
