package restaurantrush.domain;

public final class CustomerStatus {
    private CustomerStatus() { }

    public static final String WAITING = "WAITING";
    public static final String SEATED = "SEATED";
    public static final String EATING = "EATING";
    public static final String READY_TO_PAY = "READY_TO_PAY";
    public static final String PAID = "PAID";
    public static final String LEFT = "LEFT";
}
