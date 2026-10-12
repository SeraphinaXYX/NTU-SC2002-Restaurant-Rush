package restaurantrush.application;

import java.util.ArrayList;
import restaurantrush.domain.Cashier;
import restaurantrush.domain.Chef;
import restaurantrush.domain.Customer;
import restaurantrush.domain.CustomerStatus;
import restaurantrush.domain.Menu;
import restaurantrush.domain.OrderStatus;
import restaurantrush.domain.PaymentRecord;
import restaurantrush.domain.Table;
import restaurantrush.domain.Waiter;

public final class GameController {
    public static final int STAGE = 1;
    public static final int MAX_TURNS = 18;
    private final Menu menu = new Menu();
    private final ArrayList<Customer> customers = new ArrayList<Customer>();
    private final ArrayList<Table> tables = new ArrayList<Table>();
    private final ArrayList<PaymentRecord> payments = new ArrayList<PaymentRecord>();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private int turn;
    private String phase = TurnPhase.BETWEEN_TURNS;

    public GameController() {
        tables.add(new Table(1));
        tables.add(new Table(2));
    }

    public void beginTurn() {
        requirePhase(TurnPhase.BETWEEN_TURNS);
        turn++;
        waiter.startTurn();
        chef.startTurn();
        cashier.startTurn();
        if ((turn - 1) % 3 == 0) { customers.add(new Customer((turn + 2) / 3, turn)); }
        phase = TurnPhase.WAITER;
    }

    public void seatCustomer(int id) {
        requirePhase(TurnPhase.WAITER);
        waiter.seat(customer(id), freeTable());
        phase = TurnPhase.CHEF;
    }

    public void takeOrder(int id) {
        requirePhase(TurnPhase.WAITER);
        waiter.takeOrder(customer(id), menu);
        phase = TurnPhase.CHEF;
    }

    public void serveCustomer(int id) {
        requirePhase(TurnPhase.WAITER);
        waiter.serve(customer(id), turn);
        phase = TurnPhase.CHEF;
    }

    public void waitWaiter() {
        requirePhase(TurnPhase.WAITER);
        phase = TurnPhase.CHEF;
    }

    public void prepareOrder(int id) {
        requirePhase(TurnPhase.CHEF);
        chef.prepare(customer(id), turn);
        phase = TurnPhase.END_OF_TURN;
    }

    public void waitChef() {
        requirePhase(TurnPhase.CHEF);
        phase = TurnPhase.END_OF_TURN;
    }

    public void finishTurn() {
        requirePhase(TurnPhase.END_OF_TURN);
        for (Customer customer : customers) { customer.finishEating(turn); }
        PaymentRecord payment = cashier.collectOldest(customers, turn);
        if (payment != null) { payments.add(payment); }
        for (Customer customer : customers) { customer.losePatience(); }
        for (Table table : tables) { table.releaseIfFinished(); }
        if (turn == MAX_TURNS) { phase = TurnPhase.FINISHED; }
        else { phase = TurnPhase.BETWEEN_TURNS; }
    }

    private Customer customer(int id) {
        for (Customer customer : customers) {
            if (customer.getId() == id) { return customer; }
        }
        throw new IllegalArgumentException("No arrived customer with ID " + id + ".");
    }

    private Table freeTable() {
        for (Table table : tables) {
            if (table.isFree()) { return table; }
        }
        throw new IllegalStateException("No free table.");
    }

    private void requirePhase(String expected) {
        if (!phase.equals(expected)) { throw new IllegalStateException("Current phase is " + phase + "."); }
    }

    public GameResult getResult() {
        requirePhase(TurnPhase.FINISHED);
        return new GameResult(getPaidCount(), getRevenueCents(), getPaidSatisfactionTotal());
    }

    public int getRevenueCents() {
        int total = 0;
        for (PaymentRecord payment : payments) { total += payment.getBillCents(); }
        return total;
    }

    public int getPaidSatisfactionTotal() {
        int total = 0;
        for (PaymentRecord payment : payments) { total += payment.getSatisfaction(); }
        return total;
    }

    public double getAverageSatisfaction() {
        if (payments.size() == 0) { return 0; }
        return (double) getPaidSatisfactionTotal() / payments.size();
    }

    public int getServedCount() {
        int count = 0;
        for (Customer customer : customers) {
            if (customer.getOrder() != null && (customer.getOrder().getStatus().equals(OrderStatus.SERVED)
                    || customer.getOrder().getStatus().equals(OrderStatus.PAID))) { count++; }
        }
        return count;
    }

    public int getDepartureCount() {
        int count = 0;
        for (Customer customer : customers) {
            if (customer.getStatus().equals(CustomerStatus.LEFT)) { count++; }
        }
        return count;
    }

    public int getPaidCount() { return payments.size(); }
    public int getTurn() { return turn; }
    public String getPhase() { return phase; }
    public boolean isFinished() { return phase.equals(TurnPhase.FINISHED); }
    public Menu getMenu() { return menu; }
    public ArrayList<Customer> getCustomers() { return new ArrayList<Customer>(customers); }
    public ArrayList<Table> getTables() { return new ArrayList<Table>(tables); }
    public ArrayList<PaymentRecord> getPayments() { return new ArrayList<PaymentRecord>(payments); }
}
