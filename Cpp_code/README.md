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
