package restaurantrush.domain;

public final class PricingService {
    private final HappyHour happyHour;

    public PricingService(HappyHour happyHour) {
        if (happyHour == null) { throw new IllegalArgumentException("Happy Hour state is required."); }
        this.happyHour = happyHour;
    }

    public int quote(Customer customer, MenuItem item) {
        int price = customer.discountedBill(item.getPriceCents());
        if (happyHour.isActive()) { price = (price * 80 + 50) / 100; }
        return price;
    }
}
