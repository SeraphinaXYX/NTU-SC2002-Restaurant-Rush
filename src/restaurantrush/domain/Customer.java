package restaurantrush.domain;

/** Owns a customer's service flow and patience. A customer has at most one order. */
public class Customer {
    private final int id;
    private final int arrivalTurn;
    private int satisfaction = 100;
    private String status = CustomerStatus.WAITING;
    private Order order;
    private int servedTurn;
    private int readyToPayTurn;

    public Customer(int id, int arrivalTurn) {
        if (id <= 0 || arrivalTurn <= 0) { throw new IllegalArgumentException("Invalid customer identity."); }
        this.id = id;
        this.arrivalTurn = arrivalTurn;
    }

    public MenuItem chooseMeal(Menu menu) {
        return menu.getItems().get((id - 1) % menu.getItems().size());
    }

    public void seat() {
        require(status.equals(CustomerStatus.WAITING), "Only a queued customer can be seated.");
        status = CustomerStatus.SEATED;
    }

    public void placeOrder(Order newOrder) {
        require(status.equals(CustomerStatus.SEATED) && order == null, "Take one order from a seated customer.");
        if (newOrder == null || !newOrder.getStatus().equals(OrderStatus.TAKEN)) {
            throw new IllegalArgumentException("A newly taken order is required.");
        }
        order = newOrder;
    }

    public void serve(int turn) {
        require(status.equals(CustomerStatus.SEATED) && order != null, "Customer has no order to serve.");
        order.serve(turn);
        servedTurn = turn;
        status = CustomerStatus.EATING;
    }

    public void finishEating(int turn) {
        if (status.equals(CustomerStatus.EATING) && servedTurn < turn) {
            status = CustomerStatus.READY_TO_PAY;
            readyToPayTurn = turn;
        }
    }

    public boolean canPay(int turn) {
        return status.equals(CustomerStatus.READY_TO_PAY) && readyToPayTurn < turn;
    }

    public PaymentRecord pay(int turn) {
        require(canPay(turn), "Payment must follow a full READY_TO_PAY turn.");
        order.markPaid();
        status = CustomerStatus.PAID;
        return new PaymentRecord(id, order.getBillCents(), satisfaction, turn);
    }

    public void losePatience() {
        if (!status.equals(CustomerStatus.WAITING) && !status.equals(CustomerStatus.SEATED)) { return; }
        satisfaction -= getWaitingLoss();
        if (satisfaction < 0) { satisfaction = 0; }
        if (satisfaction == 0) {
            if (order != null) { order.cancel(); }
            status = CustomerStatus.LEFT;
        }
    }

    private static void require(boolean valid, String message) {
        if (!valid) { throw new IllegalStateException(message); }
    }

    public int getId() { return id; }
    public int getArrivalTurn() { return arrivalTurn; }
    public int getSatisfaction() { return satisfaction; }
    public String getStatus() { return status; }
    public Order getOrder() { return order; }
    public int getReadyToPayTurn() { return readyToPayTurn; }
    public int getWaitingLoss() { return 8; }
    public String getType() { return "Regular"; }
    public int discountedBill(int baseCents) { return baseCents; }
}
