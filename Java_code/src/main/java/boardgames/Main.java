package boardgames;

import boardgames.core.Coordinate;
import boardgames.core.PlayerRole;
import boardgames.games.checkers.AbstractCheckersGame;
import boardgames.games.checkers.CheckersConsoleRenderer;
import boardgames.games.checkers.CheckersMove;
import boardgames.session.GameDescriptor;
import boardgames.session.GameRegistry;
import boardgames.session.GameSession;

import java.util.Optional;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GameRegistry registry = new GameRegistry();

        while (true) {
            System.out.println("=========================================");
            System.out.println("    ПЛАТФОРМА НАСТІЛЬНИХ ІГОР (HOTSEAT)   ");
            System.out.println("=========================================");
            System.out.println("Оберіть гру зі списку:");

            var games = registry.getGames();
            for (int i = 0; i < games.size(); i++) {
                var desc = games.get(i);
                System.out.printf("[%d] %s%n", i + 1, desc.title());
                System.out.printf("    Правила: %s%n", desc.description());
            }
            System.out.println("[0] Вихід");
            System.out.print("\nВаш вибір: ");

            if (!scanner.hasNextInt()) break;
            int choice = scanner.nextInt();
            if (choice == 0) {
                System.out.println("Дякуємо за гру!");
                break;
            }

            if (choice < 1 || choice > games.size()) {
                System.out.println("Невірний номер, спробуйте ще раз.\n");
                continue;
            }

            GameDescriptor selected = games.get(choice - 1);
            runCheckersMatch(selected, scanner);
        }
    }

    private static void runCheckersMatch(GameDescriptor descriptor, Scanner scanner) {
        AbstractCheckersGame game = (AbstractCheckersGame) descriptor.factory().get();
        CheckersConsoleRenderer renderer = new CheckersConsoleRenderer();
        GameSession<CheckersMove> session = new GameSession<>(game);

        System.out.println("\n-----------------------------------------");
        System.out.println("Розпочато: " + game.getName());
        System.out.println("Розмір дошки: " + game.getBoard().rows() + "x" + game.getBoard().cols());
        System.out.println("Гравців: 2 (на одному пристрої)");
        System.out.println("-----------------------------------------");

        while (!session.isFinished()) {
            renderer.render(game);
            PlayerRole turn = session.getCurrentTurn();
            System.out.printf("Хід гравця: %s (%s)%n",
                    turn.getDefaultName(),
                    turn == PlayerRole.FIRST ? "білі (w/W)" : "чорні (b/B)");
            System.out.printf("Поточний рахунок | Гравець 1: %d, Гравець 2: %d%n",
                    game.getScore(PlayerRole.FIRST), game.getScore(PlayerRole.SECOND));

            System.out.print("Введіть координати [fromRow fromCol toRow toCol] (або -1 для виходу): ");
            int fromR = scanner.nextInt();
            if (fromR == -1) {
                System.out.println("Партію перервано користувачем.\n");
                return;
            }
            int fromC = scanner.nextInt();
            int toR = scanner.nextInt();
            int toC = scanner.nextInt();

            Coordinate from = new Coordinate(fromR, fromC);
            Coordinate to = new Coordinate(toR, toC);

            Optional<Coordinate> captured = Optional.empty();
            if (Math.abs(fromR - toR) == 2 && Math.abs(fromC - toC) == 2) {
                captured = Optional.of(new Coordinate((fromR + toR) / 2, (fromC + toC) / 2));
            }

            CheckersMove move = new CheckersMove(from, to, captured);
            if (!session.executeMove(move)) {
                System.out.println(" Некоректний хід! (Пам'ятайте про обов'язкове взяття)");
            }
        }

        renderer.render(game);
        System.out.println("=== ГРУ ЗАВЕРШЕНО ===");
        var res = session.getResult();
        if (res.isDraw()) {
            System.out.println("Результат: Нічия!");
        } else {
            System.out.println("Переміг: " + res.winner().orElseThrow().getDefaultName());
        }
        System.out.printf("Фінальні очки -> %s: %d | %s: %d%n%n",
                PlayerRole.FIRST.getDefaultName(), game.getScore(PlayerRole.FIRST),
                PlayerRole.SECOND.getDefaultName(), game.getScore(PlayerRole.SECOND));
    }
}