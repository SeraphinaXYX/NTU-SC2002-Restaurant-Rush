package restaurantrush.domain;

public final class Table {
    private final int id;
    private Customer occupant;

    public Table(int id) { this.id = id; }

    public void seat(Customer customer) {
        if (occupant != null) { throw new IllegalStateException("Table is occupied until payment or departure."); }
        customer.seat();
        occupant = customer;
    }

    public void releaseIfFinished() {
        if (occupant != null && (occupant.getStatus().equals(CustomerStatus.PAID)
                || occupant.getStatus().equals(CustomerStatus.LEFT))) { occupant = null; }
    }

    public int getId() { return id; }
    public Customer getOccupant() { return occupant; }
    public boolean isFree() { return occupant == null; }
}
