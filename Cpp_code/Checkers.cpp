#include "Checkers.h"
#include <cmath>



Piece::Piece(Color c, PieceType t) : color(c), type(t) {}



Board::Board() {
    grid.resize(8);
    for (int i = 0; i < 8; ++i) {
        grid[i].resize(8);
    }
    initialize();
}

void Board::initialize() {
    for (int row = 0; row < 8; ++row) {
        for (int col = 0; col < 8; ++col) {
            grid[row][col].reset();
            if ((row + col) % 2 != 0) {
                if (row < 3) {
                    grid[row][col] = std::make_unique<Piece>(Color::Black);
                } else if (row > 4) {
                    grid[row][col] = std::make_unique<Piece>(Color::White);
                }
            }
        }
    }
}

Piece* Board::getPiece(Position pos) const {
    if (!pos.isValid()) return nullptr;
    return grid[pos.row][pos.col].get();
}

bool Board::isEmpty(Position pos) const {
    return pos.isValid() && grid[pos.row][pos.col] == nullptr;
}

bool Board::evaluateRawMove(Position from, Position to, Color player, bool& isCapture, Position& capturedPos) const {
    if (!from.isValid() || !to.isValid() || !isEmpty(to)) return false;

    Piece* piece = getPiece(from);
    if (!piece || piece->getColor() != player) return false;

    int dRow = to.row - from.row;
    int dCol = to.col - from.col;

    if (dRow == 0 || std::abs(dRow) != std::abs(dCol)) return false;

    isCapture = false;
    capturedPos = {-1, -1};

    if (piece->getType() == PieceType::Man) {
        int forwardDir = (player == Color::White) ? -1 : 1;

        // Normal 1-step forward diagonal move
        if (dRow == forwardDir && std::abs(dCol) == 1) {
            return true;
        }

        // Capture jump (2 squares diagonally, forward or backward)
        if (std::abs(dRow) == 2 && std::abs(dCol) == 2) {
            Position mid{from.row + dRow / 2, from.col + dCol / 2};
            Piece* midPiece = getPiece(mid);
            if (midPiece && midPiece->getColor() != player) {
                isCapture = true;
                capturedPos = mid;
                return true;
            }
        }
    } else if (piece->getType() == PieceType::King) {
        int stepRow = (dRow > 0) ? 1 : -1;
        int stepCol = (dCol > 0) ? 1 : -1;
        int enemiesCount = 0;
        Position enemyPos{-1, -1};

        for (int r = from.row + stepRow, c = from.col + stepCol; r != to.row; r += stepRow, c += stepCol) {
            Piece* p = getPiece({r, c});
            if (p) {
                if (p->getColor() == player) {
                    return false;
                }
                enemiesCount++;
                enemyPos = {r, c};
            }
        }

        if (enemiesCount == 0) {
            return true;
        }
        if (enemiesCount == 1) {
            isCapture = true;
            capturedPos = enemyPos;
            return true;
        }
    }

    return false;
}

bool Board::hasCaptureFrom(Position from, Color player) const {
    Piece* piece = getPiece(from);
    if (!piece || piece->getColor() != player) return false;

    bool isCapture = false;
    Position capturedPos{-1, -1};

    for (int r = 0; r < 8; ++r) {
        for (int c = 0; c < 8; ++c) {
            if (evaluateRawMove(from, {r, c}, player, isCapture, capturedPos) && isCapture) {
                return true;
            }
        }
    }
    return false;
}

bool Board::hasAnyCapture(Color player) const {
    for (int r = 0; r < 8; ++r) {
        for (int c = 0; c < 8; ++c) {
            if (hasCaptureFrom({r, c}, player)) {
                return true;
            }
        }
    }
    return false;
}

std::vector<MoveInfo> Board::getLegalMovesForPiece(Position from, Color player, std::optional<Position> forcedPiece) const {
    std::vector<MoveInfo> moves;
    if (!from.isValid()) return moves;


    if (forcedPiece.has_value() && !(from == forcedPiece.value())) {
        return moves;
    }

    Piece* piece = getPiece(from);
    if (!piece || piece->getColor() != player) return moves;

    bool mustCapture = forcedPiece.has_value() || hasAnyCapture(player);

    for (int r = 0; r < 8; ++r) {
        for (int c = 0; c < 8; ++c) {
            Position to{r, c};
            bool isCapture = false;
            Position capturedPos{-1, -1};

            if (evaluateRawMove(from, to, player, isCapture, capturedPos)) {
                if (mustCapture && !isCapture) {
                    continue;
                }
                moves.push_back(MoveInfo{to, isCapture, capturedPos});
            }
        }
    }

    return moves;
}

bool Board::movePiece(Position from, Position to, Color currentPlayer,
                      std::optional<Position> forcedPiece, bool& continuedCapture) {
    continuedCapture = false;
    std::vector<MoveInfo> legalMoves = getLegalMovesForPiece(from, currentPlayer, forcedPiece);

    const MoveInfo* chosenMove = nullptr;
    for (const auto& m : legalMoves) {
        if (m.to == to) {
            chosenMove = &m;
            break;
        }
    }

    if (!chosenMove) {
        return false;
    }

    grid[to.row][to.col] = std::move(grid[from.row][from.col]);

    if (chosenMove->isCapture) {
        grid[chosenMove->capturedPos.row][chosenMove->capturedPos.col].reset();
    }

    Piece* movedPiece = getPiece(to);
    if (movedPiece->getType() == PieceType::Man) {
        if ((currentPlayer == Color::White && to.row == 0) ||
            (currentPlayer == Color::Black && to.row == 7)) {
            movedPiece->promote();
        }
    }


    if (chosenMove->isCapture && hasCaptureFrom(to, currentPlayer)) {
        continuedCapture = true;
    }

    return true;
}

int Board::countPieces(Color player) const {
    int count = 0;
    for (int r = 0; r < 8; ++r) {
        for (int c = 0; c < 8; ++c) {
            if (grid[r][c] && grid[r][c]->getColor() == player) {
                count++;
            }
        }
    }
    return count;
}

bool Board::hasValidMoves(Color player) const {
    for (int r = 0; r < 8; ++r) {
        for (int c = 0; c < 8; ++c) {
            Position pos{r, c};
            Piece* p = getPiece(pos);
            if (p && p->getColor() == player) {
                if (!getLegalMovesForPiece(pos, player, std::nullopt).empty()) {
                    return true;
                }
            }
        }
    }
    return false;
}



Game::Game()
    : window(sf::VideoMode(static_cast<unsigned int>(BOARD_SIZE * TILE_SIZE),
                           static_cast<unsigned int>(BOARD_SIZE * TILE_SIZE)),
             "Checkers", sf::Style::Titlebar | sf::Style::Close),
      currentTurn(Color::White),
      isGameOver(false) {
    window.setFramerateLimit(60);
    updateWindowTitle();
}

void Game::run() {
    while (window.isOpen()) {
        processEvents();
        render();
    }
}

void Game::processEvents() {
    sf::Event event;
    while (window.pollEvent(event)) {
        if (event.type == sf::Event::Closed) {
            window.close();
        } else if (event.type == sf::Event::KeyPressed) {
            if (event.key.code == sf::Keyboard::R) {
                resetGame();
            }
        } else if (event.type == sf::Event::MouseButtonPressed && !isGameOver) {
            if (event.mouseButton.button == sf::Mouse::Left) {
                handleMouseClick(event.mouseButton.x, event.mouseButton.y);
            }
        }
    }
}

void Game::refreshHighlightedMoves() {
    highlightedMoves.clear();
    if (selectedPos.has_value()) {
        highlightedMoves = board.getLegalMovesForPiece(selectedPos.value(), currentTurn, forcedPiecePos);
    }
}

void Game::handleMouseClick(int x, int y) {
    int col = static_cast<int>(x / TILE_SIZE);
    int row = static_cast<int>(y / TILE_SIZE);
    Position clickedPos{row, col};

    if (!clickedPos.isValid()) return;


    if (selectedPos.has_value()) {
        bool isTargetMove = false;
        for (const auto& m : highlightedMoves) {
            if (m.to == clickedPos) {
                isTargetMove = true;
                break;
            }
        }

        if (isTargetMove) {
            bool continuedCapture = false;
            if (board.movePiece(selectedPos.value(), clickedPos, currentTurn, forcedPiecePos, continuedCapture)) {
                if (continuedCapture) {
                    // Keep turn and lock selection onto the capturing piece for the next jump
                    forcedPiecePos = clickedPos;
                    selectedPos = clickedPos;
                    refreshHighlightedMoves();
                } else {
                    forcedPiecePos.reset();
                    selectedPos.reset();
                    highlightedMoves.clear();
                    switchTurn();

                    if (!board.hasValidMoves(currentTurn)) {
                        isGameOver = true;
                    }
                }
                updateWindowTitle();
                return;
            }
        }
    }


    if (forcedPiecePos.has_value()) {
        selectedPos = forcedPiecePos;
        refreshHighlightedMoves();
        return;
    }

    Piece* clickedPiece = board.getPiece(clickedPos);
    if (clickedPiece && clickedPiece->getColor() == currentTurn) {
        selectedPos = clickedPos;
        refreshHighlightedMoves();
    } else {
        selectedPos.reset();
        highlightedMoves.clear();
    }
}

void Game::render() {
    window.clear();

    sf::RectangleShape tile(sf::Vector2f(TILE_SIZE, TILE_SIZE));
    const sf::Color lightSquare(238, 238, 210);
    const sf::Color darkSquare(118, 150, 86);
    const sf::Color highlightSquare(186, 202, 68);
    const sf::Color forcedSquare(214, 120, 50);

    // Draw board squares and pieces
    for (int row = 0; row < BOARD_SIZE; ++row) {
        for (int col = 0; col < BOARD_SIZE; ++col) {
            tile.setPosition(col * TILE_SIZE, row * TILE_SIZE);

            if (forcedPiecePos.has_value() && forcedPiecePos->row == row && forcedPiecePos->col == col) {
                tile.setFillColor(forcedSquare);
            } else if (selectedPos.has_value() && selectedPos->row == row && selectedPos->col == col) {
                tile.setFillColor(highlightSquare);
            } else if ((row + col) % 2 == 0) {
                tile.setFillColor(lightSquare);
            } else {
                tile.setFillColor(darkSquare);
            }
            window.draw(tile);

            // Draw piece if present
            Piece* piece = board.getPiece({row, col});
            if (piece) {
                float radius = TILE_SIZE * 0.38f;
                sf::CircleShape circle(radius);
                circle.setOrigin(radius, radius);
                circle.setPosition(col * TILE_SIZE + TILE_SIZE / 2.f,
                                   row * TILE_SIZE + TILE_SIZE / 2.f);

                if (piece->getColor() == Color::White) {
                    circle.setFillColor(sf::Color(245, 245, 245));
                    circle.setOutlineColor(sf::Color(40, 40, 40));
                } else {
                    circle.setFillColor(sf::Color(35, 35, 35));
                    circle.setOutlineColor(sf::Color(200, 200, 200));
                }

                if (piece->getType() == PieceType::King) {
                    circle.setOutlineColor(sf::Color(255, 215, 0));
                    circle.setOutlineThickness(6.f);
                } else {
                    circle.setOutlineThickness(3.f);
                }

                window.draw(circle);
            }
        }
    }

    // Draw legal move indicators for the selected piece
    for (const auto& move : highlightedMoves) {
        float markerRadius = move.isCapture ? TILE_SIZE * 0.18f : TILE_SIZE * 0.13f;
        sf::CircleShape marker(markerRadius);
        marker.setOrigin(markerRadius, markerRadius);
        marker.setPosition(move.to.col * TILE_SIZE + TILE_SIZE / 2.f,
                           move.to.row * TILE_SIZE + TILE_SIZE / 2.f);

        if (move.isCapture) {
            marker.setFillColor(sf::Color(220, 50, 47, 210));
            marker.setOutlineColor(sf::Color(255, 255, 255, 220));
            marker.setOutlineThickness(2.f);
        } else {
            marker.setFillColor(sf::Color(30, 120, 220, 190));
            marker.setOutlineColor(sf::Color(255, 255, 255, 200));
            marker.setOutlineThickness(2.f);
        }

        window.draw(marker);
    }

    window.display();
}

void Game::switchTurn() {
    currentTurn = (currentTurn == Color::White) ? Color::Black : Color::White;
}

void Game::updateWindowTitle() {
    if (isGameOver) {
        std::string winner = (currentTurn == Color::White) ? "Black" : "White";
        window.setTitle("Checkers - Game Over! " + winner + " wins! (Press R to restart)");
    } else if (forcedPiecePos.has_value()) {
        std::string turnStr = (currentTurn == Color::White) ? "White" : "Black";
        window.setTitle("Checkers - " + turnStr + " multi-capture in progress! (Press R to restart)");
    } else {
        std::string turnStr = (currentTurn == Color::White) ? "White's Turn" : "Black's Turn";
        window.setTitle("Checkers - " + turnStr + " (Press R to restart)");
    }
}

void Game::resetGame() {
    board.initialize();
    currentTurn = Color::White;
    isGameOver = false;
    selectedPos.reset();
    forcedPiecePos.reset();
    highlightedMoves.clear();
    updateWindowTitle();
}