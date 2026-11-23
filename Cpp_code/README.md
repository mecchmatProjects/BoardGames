# Board Games (C++17 + SFML)

Two SFML-based board games: **Checkers** and **Chess**. The chess game supports a
Stockfish bot via the UCI protocol. Ready-to-run Stockfish binaries for both
platforms are bundled in this folder (`stockfish linux/` on Linux, `stockfish
windows/` on Windows), and the default engine path follows the platform
automatically; it can also be changed in-game with the `F` key.

A C++17 compiler (g++/clang++/MSVC) and CMake are required; SFML 2.6.1 is fetched
automatically by CMake. NOTE: a fresh SFML build additionally needs the X11/OpenGL
dev headers; on this machine g++/clang++/CMake exist but those headers do not, yet
`build/` already contains prebuilt SFML libraries, so the games are already built.

# Checkers

A console checkers game for two local players, implemented in C++17.

## Rules

- Uses the 8x8 English/American checkers board.
- Men move and capture diagonally forward; kings move and capture in both directions.
- Captures are mandatory. A piece must continue capturing while another jump is available.
- A man is crowned upon reaching the opposite back row. The turn ends on crowning.
- A player wins when the opponent has no pieces or no legal moves.

## Build and run

With CMake:

```text
cmake -S . -B build
cmake --build build
```

Or compile directly with a C++17 compiler:

```text
g++ -std=c++17 -Wall -Wextra -pedantic main.cpp Checkers.cpp -o checkers
```

Enter moves as a start square followed by a destination square, for example `b6 a5`.
Type `help` for a reminder or `quit` to exit.

## Chess (SFML + optional Stockfish bot)

Build:

```text
cmake -S . -B build
cmake --build build
./build/chess     # or the generated .exe on Windows
```

Controls:
- Click a piece, then click one of the highlighted squares to move (highlights show
  legal moves, red for captures, gold for promotions).
- `B` toggles the Stockfish bot (the bot always plays Black).
- `F` opens typed input for the engine binary path, then press Enter
  (default is the bundled engine for the current platform: `./stockfish linux/stockfish-linux-x86-64-universal`
  on Linux, `.\stockfish windows\stockfish-windows-x86-64-universal.exe` on Windows).
- `R` restarts the game.

Promotion is automatic (always a queen). Castling, en passant, check/checkmate,
stalemate, fifty-move rule and insufficient-material draws are handled.

Pieces are rendered as unicode chess glyphs when the loaded font has them (candidate
fonts without those glyphs are rejected, so you never get identical tofu squares);
otherwise each type falls back to its own silhouette texture (round-headed pawn,
crenellated rook, eared knight, ball-topped bishop, three-pearl queen, cross-topped king).

Stockfish 19 binaries are bundled in this repository folder (`https://stockfish-chess.org
is the upstream source if you prefer your own build): the game picks the right one per
platform automatically. On POSIX the engine is spawned with fork + execvp over anonymous
pipes; on Windows with CreatePipe + CreateProcess and STARTUPINFO stdio redirection, so
engine paths containing a space (like `stockfish windows\...`) work on both platforms.
Each search starts a fresh, stateless engine session on a background thread (1 s per move);
cancelling a search kills the engine process (SIGKILL / TerminateProcess).
