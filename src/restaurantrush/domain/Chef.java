package restaurantrush.domain;

public final class Chef extends Staff {
    public void prepare(Customer customer, int turn) {
        requireAvailable();
        if (customer.getOrder() == null) { throw new IllegalStateException("Customer has not placed an order."); }
        customer.getOrder().prepare(turn);
        completeTask();
    }
}
