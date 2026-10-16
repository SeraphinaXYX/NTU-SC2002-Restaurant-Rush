package restaurantrush.application;

import restaurantrush.domain.Customer;
import restaurantrush.domain.CriticCustomer;
import restaurantrush.domain.VIPCustomer;

public final class ArrivalSchedule {
    public Customer arrivalAt(int turn) {
        if (turn < 1 || turn > 18 || (turn - 1) % 3 != 0) { return null; }
        int id = (turn + 2) / 3;
        if (id == 2) { return new VIPCustomer(id, turn); }
        if (id == 3) { return new CriticCustomer(id, turn); }
        return new Customer(id, turn);
    }
}
