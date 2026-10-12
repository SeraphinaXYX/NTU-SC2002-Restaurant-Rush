package restaurantrush.ui;

import java.util.Scanner;
import restaurantrush.application.GameController;
import restaurantrush.application.TurnPhase;
import restaurantrush.domain.Customer;
import restaurantrush.domain.CustomerStatus;
import restaurantrush.domain.MenuItem;
import restaurantrush.domain.Order;
import restaurantrush.domain.Table;

public final class GameCLI {
    private final GameController game;
    private final Scanner input;
    private boolean inputEnded;

    public GameCLI(GameController game, Scanner input) {
        this.game = game;
        this.input = input;
    }

    public void run() {
        System.out.println("Restaurant Rush - Stage " + GameController.STAGE);
        System.out.println("Goal after turn 18: 4 paid customers, $45.00 revenue, average satisfaction 60.");
        for (MenuItem item : game.getMenu().getItems()) {
            System.out.println(item.getName() + ": " + money(item.getPriceCents())
                    + ", preparation units " + item.getPreparationUnits());
        }
        while (!game.isFinished()) {
            game.beginTurn();
            displayState();

            while (game.getPhase().equals(TurnPhase.WAITER)) {
                System.out.print("Waiter [seat ID | order ID | serve ID | wait]: ");
                choose(readChoice("wait"), TurnPhase.WAITER);
            }
            while (game.getPhase().equals(TurnPhase.CHEF)) {
                System.out.print("Chef [cook ID | wait]: ");
                choose(readChoice("wait"), TurnPhase.CHEF);
            }
            game.finishTurn();
            System.out.println("End turn " + game.getTurn() + ": " + summary());
        }
        if (game.getResult().isVictory()) { System.out.println("FINAL: VICTORY"); }
        else { System.out.println("FINAL: DEFEAT"); }
        System.out.println(summary() + " | Turns used: " + game.getTurn());
        displayCustomers();
    }


    private void choose(String line, String role) {
        Scanner words = new Scanner(line);
        try {
            if (!words.hasNext()) { throw new IllegalArgumentException("Enter a staff task."); }
            String task = words.next();
            if (task.equals("wait") && !words.hasNext()) {
                if (role.equals(TurnPhase.CHEF)) { game.waitChef(); }

                else { game.waitWaiter(); }
                return;
            }
            if (!words.hasNextInt()) { throw new IllegalArgumentException("Enter a task and a numeric customer ID."); }
            int id = words.nextInt();
            if (words.hasNext()) { throw new IllegalArgumentException("Too many arguments."); }
            if (role.equals(TurnPhase.CHEF) && task.equals("cook")) { game.prepareOrder(id); }

            else if (role.equals(TurnPhase.WAITER) && task.equals("seat")) { game.seatCustomer(id); }
            else if (role.equals(TurnPhase.WAITER) && task.equals("order")) { game.takeOrder(id); }
            else if (role.equals(TurnPhase.WAITER) && task.equals("serve")) { game.serveCustomer(id); }
            else { throw new IllegalArgumentException("That task is not available to this staff member."); }
        } catch (IllegalArgumentException invalid) {
            System.out.println("Invalid choice: " + invalid.getMessage() + " Try again; no task was used.");
        } catch (IllegalStateException invalid) {
            System.out.println("Invalid choice: " + invalid.getMessage() + " Try again; no task was used.");
        } finally {
            words.close();
        }
    }

    private String readChoice(String fallback) {
        while (input.hasNextLine()) {
            String line = input.nextLine().trim();
            if (line.length() > 0 && line.charAt(0) == '#') { continue; }
            return line;
        }
        if (!inputEnded) {
            System.out.println("Input ended; remaining staff choices will wait through turn 18.");
            inputEnded = true;
        }
        return fallback;
    }

    private void displayState() {
        System.out.println("\nTurn " + game.getTurn() + "/18 | " + summary());
        String queue = "";
        for (Customer customer : game.getCustomers()) {
            if (customer.getStatus().equals(CustomerStatus.WAITING)) { queue += "C" + customer.getId() + " "; }
        }
        if (queue.length() == 0) { queue = "empty"; }
        System.out.println("Waiting queue: " + queue);
        for (Table table : game.getTables()) {
            if (table.isFree()) { System.out.println("Table " + table.getId() + ": free"); }
            else { System.out.println("Table " + table.getId() + ": C" + table.getOccupant().getId()); }
        }
        displayCustomers();

        System.out.println("Available staff: Waiter 1 task; Chef 1 prep unit; Cashier 1 automatic eligible payment.");
    }

    private void displayCustomers() {
        for (Customer customer : game.getCustomers()) {
            System.out.print("C" + customer.getId() + " " + customer.getType() + " | " + customer.getStatus()
                    + " | satisfaction " + customer.getSatisfaction() + " | ");
            Order order = customer.getOrder();
            if (order == null) { System.out.println("no order"); }
            else {
                System.out.println(order.getItem().getName() + " " + order.getStatus()
                        + ", remaining prep " + order.getRemainingUnits() + ", locked bill " + money(order.getBillCents()));
            }
        }
    }

    private String summary() {
        String average = "N/A";
        if (game.getPaidCount() > 0) { average = "" + game.getAverageSatisfaction(); }
        return "Paid: " + game.getPaidCount() + " | Served: " + game.getServedCount()
                + " | Unhappy departures: " + game.getDepartureCount() + " | Revenue: " + money(game.getRevenueCents())
                + " | Paid satisfaction: " + average;
    }

    private static String money(int cents) {
        String fraction = "" + cents % 100;
        if (cents % 100 < 10) { fraction = "0" + fraction; }
        return "$" + cents / 100 + "." + fraction;
    }
}
