package restaurantrush.domain;

/** Owns preparation progress, the locked bill and legal food-state transitions. */
public final class Order {
    private final MenuItem item;
    private final int billCents;
    private int remainingUnits;
    private int readyTurn;
    private String status = OrderStatus.TAKEN;

    public Order(MenuItem item, int billCents) {
        if (item == null || billCents <= 0) {
            throw new IllegalArgumentException("An order needs a meal and a positive bill.");
        }
        this.item = item;
        this.billCents = billCents;
        this.remainingUnits = item.getPreparationUnits();
    }

    public void prepare(int turn) {
        if (!status.equals(OrderStatus.TAKEN) && !status.equals(OrderStatus.PREPARING)) {
            throw new IllegalStateException("Only an unfinished order can be prepared.");
        }
        if (turn < 1) { throw new IllegalArgumentException("Turn must be positive."); }
        remainingUnits--;
        if (remainingUnits == 0) { status = OrderStatus.READY; }
        else { status = OrderStatus.PREPARING; }
        if (status.equals(OrderStatus.READY)) { readyTurn = turn; }
    }

    public void serve(int turn) {
        if (!status.equals(OrderStatus.READY) || readyTurn >= turn) {
            throw new IllegalStateException("Serve a READY dish from an earlier turn.");
        }
        status = OrderStatus.SERVED;
    }

    public void markPaid() {
        if (!status.equals(OrderStatus.SERVED)) {
            throw new IllegalStateException("Only a served order can be paid.");
        }
        status = OrderStatus.PAID;
    }

    public void cancel() {
        if (status.equals(OrderStatus.SERVED) || status.equals(OrderStatus.PAID) || status.equals(OrderStatus.CANCELLED)) {
            throw new IllegalStateException("Only an unserved order can be cancelled.");
        }
        status = OrderStatus.CANCELLED;
    }

    public MenuItem getItem() { return item; }
    public int getBillCents() { return billCents; }
    public int getRemainingUnits() { return remainingUnits; }
    public int getReadyTurn() { return readyTurn; }
    public String getStatus() { return status; }
}
