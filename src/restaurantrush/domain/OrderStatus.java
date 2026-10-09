package restaurantrush.domain;

public final class OrderStatus {
    private OrderStatus() { }

    public static final String TAKEN = "TAKEN";
    public static final String PREPARING = "PREPARING";
    public static final String READY = "READY";
    public static final String SERVED = "SERVED";
    public static final String PAID = "PAID";
    public static final String CANCELLED = "CANCELLED";
}
