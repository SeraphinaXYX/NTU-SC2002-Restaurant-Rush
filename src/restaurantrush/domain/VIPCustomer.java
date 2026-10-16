package restaurantrush.domain;

public final class VIPCustomer extends Customer {
    public VIPCustomer(int id, int arrivalTurn) { super(id, arrivalTurn); }

    public int getWaitingLoss() { return 5; }
    public String getType() { return "VIP"; }
    public int discountedBill(int baseCents) {
        return (baseCents * 90 + 50) / 100;
    }
}
