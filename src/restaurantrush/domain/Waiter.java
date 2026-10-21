package restaurantrush.domain;

public final class Waiter extends Staff {
    private final PricingService pricing;

    public Waiter(PricingService pricing) {
        if (pricing == null) { throw new IllegalArgumentException("Pricing is required."); }
        this.pricing = pricing;
    }

    public void seat(Customer customer, Table table) {
        requireAvailable();
        table.seat(customer);
        completeTask();
    }

    public void takeOrder(Customer customer, Menu menu) {
        requireAvailable();
        MenuItem item = customer.chooseMeal(menu);
        customer.placeOrder(new Order(item, pricing.quote(customer, item)));
        completeTask();
    }

    public void serve(Customer customer, int turn) {
        requireAvailable();
        customer.serve(turn);
        completeTask();
    }
}
