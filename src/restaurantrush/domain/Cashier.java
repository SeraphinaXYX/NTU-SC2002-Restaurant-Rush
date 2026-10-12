package restaurantrush.domain;

import java.util.ArrayList;

public final class Cashier extends Staff {
    public PaymentRecord collectOldest(ArrayList<Customer> customers, int turn) {
        requireAvailable();
        Customer next = null;
        for (Customer customer : customers) {
            if (customer.canPay(turn)) {
                if (next == null || customer.getReadyToPayTurn() < next.getReadyToPayTurn()
                        || (customer.getReadyToPayTurn() == next.getReadyToPayTurn()
                        && customer.getId() < next.getId())) {
                    next = customer;
                }
            }
        }
        if (next == null) { return null; }
        PaymentRecord record = next.pay(turn);
        completeTask();
        return record;
    }
}
