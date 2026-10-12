package restaurantrush.application;

/** Final result uses unrounded totals; display rounding cannot turn a defeat into victory. */
public final class GameResult {
    private final int paidCustomers;
    private final int revenueCents;
    private final int satisfactionTotal;

    public GameResult(int paidCustomers, int revenueCents, int satisfactionTotal) {
        this.paidCustomers = paidCustomers;
        this.revenueCents = revenueCents;
        this.satisfactionTotal = satisfactionTotal;
    }

    public boolean isVictory() {
        return paidCustomers >= 4 && revenueCents >= 4500 && satisfactionTotal >= 60L * paidCustomers;
    }

    public int getPaidCustomers() { return paidCustomers; }
    public int getRevenueCents() { return revenueCents; }
    public int getSatisfactionTotal() { return satisfactionTotal; }
}
