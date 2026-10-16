package restaurantrush.domain;

public final class Waiter extends Staff {
    public void seat(Customer customer, Table table) {
        requireAvailable();
        table.seat(customer);
        completeTask();
    }

    public void takeOrder(Customer customer, Menu menu) {
        requireAvailable();
        MenuItem item = customer.chooseMeal(menu);
        customer.placeOrder(new Order(item, customer.discountedBill(item.getPriceCents())));
        completeTask();
    }

    public void serve(Customer customer, int turn) {
        requireAvailable();
        customer.serve(turn);
        completeTask();
    }
}
