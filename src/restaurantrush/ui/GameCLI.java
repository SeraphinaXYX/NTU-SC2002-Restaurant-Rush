package restaurantrush.ui;

import java.util.Scanner;
import restaurantrush.application.GameController;
import restaurantrush.application.TurnPhase;
import restaurantrush.domain.Customer;
import restaurantrush.domain.CustomerStatus;
import restaurantrush.domain.MenuItem;
import restaurantrush.domain.Order;
import restaurantrush.domain.OrderStatus;
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
            beginTurn();
            displayState();

            while (game.getPhase().equals(TurnPhase.HOST)) {
                System.out.print("Host [seat ID | wait]: ");
                choose(readChoice("wait"), TurnPhase.HOST);
            }

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


    private void beginTurn() {
        while (true) {
            System.out.print("Start turn " + (game.getTurn() + 1) + ": Happy Hour [on | off]: ");
            String choice = readChoice("off");
            try {
                if (!choice.equals("on") && !choice.equals("off")) {
                    throw new IllegalArgumentException("Choose on or off.");
                }
                game.beginTurn(choice.equals("on"));
                return;
            } catch (IllegalArgumentException invalid) {
                System.out.println("Invalid choice: " + invalid.getMessage() + " Try again.");
            } catch (IllegalStateException invalid) {
                System.out.println("Invalid choice: " + invalid.getMessage() + " Turn has not started; try again.");
            }
        }
    }

    private void choose(String line, String role) {
        Scanner words = new Scanner(line);
        try {
            if (!words.hasNext()) { throw new IllegalArgumentException("Enter a staff task."); }
            String task = words.next();
            if (task.equals("wait") && !words.hasNext()) {
                if (role.equals(TurnPhase.CHEF)) { game.waitChef(); }
                else if (role.equals(TurnPhase.HOST)) { game.waitHost(); }
                else { game.waitWaiter(); }
                return;
            }
            if (!words.hasNextInt()) { throw new IllegalArgumentException("Enter a task and a numeric customer ID."); }
            int id = words.nextInt();
            if (words.hasNext()) { throw new IllegalArgumentException("Too many arguments."); }
            if (role.equals(TurnPhase.CHEF) && task.equals("cook")) { game.prepareOrder(id); }
            else if (role.equals(TurnPhase.HOST) && task.equals("seat")) { game.seatWithHost(id); }
            else if (role.equals(TurnPhase.WAITER) && task.equals("seat")) { game.seatCustomer(id); }
            else if (role.equals(TurnPhase.WAITER) && task.equals("order")) {
                game.takeOrder(id);
                for (Customer customer : game.getCustomers()) {
                    if (customer.getId() == id) {
                        Order order = customer.getOrder();
                        System.out.println("Order placed for C" + id + ": " + order.getItem().getName()
                                + ", preparation units " + order.getItem().getPreparationUnits()
                                + ", locked bill " + money(order.getBillCents()));
                        break;
                    }
                }
            }
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
        System.out.println("Happy Hour remaining: " + game.getHappyHourRemainingTurns() + " active turns including this turn.");
        System.out.println("Available staff: Host 1 seating; Waiter 1 task; Chef 1 prep unit; Cashier 1 automatic eligible payment.");
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
            String timing = timingHint(customer);
            if (timing.length() > 0) { System.out.println("  Timing: " + timing); }
        }
    }

    private String timingHint(Customer customer) {
        Order order = customer.getOrder();
        if (order == null || customer.getStatus().equals(CustomerStatus.PAID)
                || customer.getStatus().equals(CustomerStatus.LEFT)) { return ""; }
        if (customer.getStatus().equals(CustomerStatus.EATING)) {
            return paymentTiming(customer.getServedTurn() + 2) + ".";
        }
        if (customer.getStatus().equals(CustomerStatus.READY_TO_PAY)) {
            if (game.isFinished()) { return "Unpaid at closing; the Cashier handles one eligible customer per turn."; }
            int paymentTurn = customer.getReadyToPayTurn() + 1;
            if (paymentTurn <= game.getTurn()) {
                return "Eligible for automatic payment this turn; the Cashier handles one customer.";
            }
            return paymentTiming(paymentTurn) + ".";
        }
        if (game.isFinished()) { return "Unserved at closing; no payment recorded."; }
        if (order.getStatus().equals(OrderStatus.READY)) {
            int serveTurn = order.getReadyTurn() + 1;
            if (serveTurn <= game.getTurn()) {
                return "Can serve now (serve " + customer.getId() + "); "
                        + paymentTiming(game.getTurn() + 2) + " if served now.";
            }
            return "Serve from turn " + serveTurn + "; " + paymentTiming(serveTurn + 2) + " if served then.";
        }
        int serveTurn = game.getTurn() + order.getRemainingUnits();
        return "Earliest serve: turn " + serveTurn + " if cooked each turn; "
                + paymentTiming(serveTurn + 2) + " if served then.";
    }

    private static String paymentTiming(int turn) {
        String text = "earliest payment: turn " + turn;
        if (turn > GameController.MAX_TURNS) { text += " (after closing at turn " + GameController.MAX_TURNS + ")"; }
        return text;
    }

    private String summary() {
        return "Paid: " + game.getPaidCount() + " | Served: " + game.getServedCount()
                + " | Unhappy departures: " + game.getDepartureCount() + " | Revenue: " + money(game.getRevenueCents())
                + " | Paid satisfaction: " + averageSatisfaction();
    }

    private String averageSatisfaction() {
        int count = game.getPaidCount();
        if (count == 0) { return "N/A"; }
        // Round the displayed average to tenths; the game still uses exact totals.
        int tenths = (game.getPaidSatisfactionTotal() * 10 + count / 2) / count;
        return tenths / 10 + "." + tenths % 10;
    }

    private static String money(int cents) {
        String fraction = "" + cents % 100;
        if (cents % 100 < 10) { fraction = "0" + fraction; }
        return "$" + cents / 100 + "." + fraction;
    }
}
