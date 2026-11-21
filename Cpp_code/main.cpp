#include "Checkers.h"

#include <algorithm>
#include <cctype>
#include <iostream>
#include <string>
#include <vector>

namespace {

using checkers::Game;
using checkers::Move;
using checkers::Player;
using checkers::Position;

void printBoard(const Game& game) {
    const auto& board = game.board();
    std::cout << "\n    a b c d e f g h\n"
              << "   +-----------------+\n";
    for (int row = 0; row < 8; ++row) {
        std::cout << ' ' << 8 - row << " |";
        for (int column = 0; column < 8; ++column) {
            const char square = board[row][column];
            if (square == '.' && (row + column) % 2 == 0) {
                std::cout << "  ";
            } else {
                std::cout << ' ' << square;
            }
        }
        std::cout << " | " << 8 - row << '\n';
    }
    std::cout << "   +-----------------+\n"
              << "    a b c d e f g h\n"
              << "Pieces: r/b = man, R/B = king, . = empty\n";
}

std::string coordinateName(Position position) {
    std::string coordinate;
    coordinate += static_cast<char>('a' + position.column);
    coordinate += static_cast<char>('8' - position.row);
    return coordinate;
}

void printHelp() {
    std::cout << "Enter a move as: <start> <destination>, for example b6 a5.\n"
              << "Commands: help, quit\n"
              << "Captures are mandatory. If a capture can continue, move that same piece.\n";
}

}  // namespace

int main() {
    Game game;
    std::cout << "CHECKERS - English/American rules (8x8)\n";
    printHelp();

    std::string input;
    while (!game.gameOver()) {
        printBoard(game);
        std::cout << "Player " << Game::playerName(game.currentPlayer()) << "'s turn";
        if (game.isForcedCapture()) {
            const auto forced = game.forcedPiece();
            std::cout << " (continue capturing with " << coordinateName(*forced) << ')';
        }
        std::cout << ". Move: ";

        if (!(std::cin >> input)) {
            std::cout << "\nInput closed. Game ended.\n";
            return 0;
        }
        std::transform(input.begin(), input.end(), input.begin(), [](unsigned char character) {
            return static_cast<char>(std::tolower(character));
        });

        if (input == "quit" || input == "exit") {
            std::cout << "Game ended by player.\n";
            return 0;
        }
        if (input == "help") {
            printHelp();
            continue;
        }

        std::string destinationText;
        if (!(std::cin >> destinationText)) {
            std::cout << "Enter both a start and destination square.\n";
            return 0;
        }
        std::transform(destinationText.begin(), destinationText.end(),
                       destinationText.begin(), [](unsigned char character) {
                           return static_cast<char>(std::tolower(character));
                       });

        const auto from = Game::parsePosition(input.c_str());
        const auto to = Game::parsePosition(destinationText.c_str());
        if (!from.has_value() || !to.has_value()) {
            std::cout << "Invalid coordinates. Use columns a-h and rows 1-8.\n";
            continue;
        }

        const std::vector<Move> moves = game.legalMovesFrom(*from);
        const auto selected = std::find_if(moves.begin(), moves.end(), [&to](const Move& move) {
            return move.to == *to;
        });
        if (selected == moves.end() || !game.makeMove(*selected)) {
            std::cout << "Illegal move. Check the piece, direction, and mandatory captures.\n";
        }
    }

    printBoard(game);
    std::cout << "Player " << Game::playerName(*game.winner()) << " wins!\n";
    return 0;
}
