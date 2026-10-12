package restaurantrush;

import java.util.Scanner;
import restaurantrush.application.GameController;
import restaurantrush.ui.GameCLI;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        GameCLI game = new GameCLI(new GameController(), input);
        game.run();
        input.close();
    }
}
