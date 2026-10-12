package restaurantrush.domain;

/** Immutable evidence of a completed payment; unpaid orders never enter revenue. */
public final class PaymentRecord {
    private final int customerId;
    private final int billCents;
    private final int satisfaction;
    private final int turn;

    public PaymentRecord(int customerId, int billCents, int satisfaction, int turn) {
        this.customerId = customerId;
        this.billCents = billCents;
        this.satisfaction = satisfaction;
        this.turn = turn;
    }

    public int getCustomerId() { return customerId; }
    public int getBillCents() { return billCents; }
    public int getSatisfaction() { return satisfaction; }
    public int getTurn() { return turn; }
}
