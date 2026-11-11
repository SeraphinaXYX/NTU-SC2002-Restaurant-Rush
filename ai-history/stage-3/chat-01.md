# AI Chat History — PricingService Chained Discounts — 27 October 2026

---

**User:** For the report design section I need to explain how Stage 3 pricing works. There's now a PricingService that applies both a customer discount and a Happy Hour discount. How does the chaining actually work?

**Assistant:** `PricingService.quote(Customer customer, MenuItem item)` applies the discounts in sequence. First it calls `customer.discountedBill(item.getPriceCents())` to get the customer-adjusted price — VIPs get 10% off, everyone else gets full price. Then, if `happyHour.isActive()`, it applies a further 20% reduction: `price = (price * 80 + 50) / 100`. The result is the final bill in cents.

The chaining means the discounts compound rather than add. A VIP during Happy Hour pays 90% of base price, then 80% of that — effectively 72% of the base price, not 70%. Whether that's intentional is a design decision, but it's what the implementation produces. For your report, the key design point is that `PricingService` depends on `HappyHour` via constructor injection, so it's independently testable: you can pass a `HappyHour` object in a known state and verify the output without running a full game.

---

**User:** Why inject HappyHour instead of just passing a boolean isActive flag into quote()?

**Assistant:** Injecting the `HappyHour` object gives `PricingService` a stable reference to the current discount state without coupling it to `GameController`. If you passed `isActive` as a boolean parameter, every caller of `quote()` would need to query the happy hour state before calling — that logic would leak into `Waiter` or wherever `quote()` is called from. By holding a reference, `PricingService` can always ask `happyHour.isActive()` itself.

It also follows the dependency inversion principle from the SC2002 course: `PricingService` depends on the `HappyHour` abstraction (the object's interface), not on `GameController`'s internal state. For your design section writeup, this is a concrete example of favouring composition and injection over direct coupling.

---
