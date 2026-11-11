# AI Chat History — ComboMeal Extending MenuItem — 29 October 2026

---

**User:** I'm implementing `ComboMeal extends MenuItem`. The combo should bundle two items at a 10% discount and its preparation time should be the sum of both components. How do I pass the right values to `super()`?

**Assistant:** Compute the name, price, and preparation units inline in the `super()` call. The tricky part is that `super()` must be the first statement in the constructor, so you can't declare variables before it. You can call a static helper method from within the argument list:

```java
public ComboMeal(MenuItem first, MenuItem second) {
    super(
        component(first).getName() + " + " + component(second).getName(),
        ((first.getPriceCents() + second.getPriceCents()) * 90 + 50) / 100,
        first.getPreparationUnits() + second.getPreparationUnits()
    );
    this.first = first;
    this.second = second;
}

private static MenuItem component(MenuItem item) {
    if (item == null) throw new IllegalArgumentException("A combo needs two components.");
    return item;
}
```

The `component()` helper validates null before the values are used in the name concatenation, since you can't throw from a null check inline in `super(...)` without it. After the `super()` call, store both components as fields so `getComponents()` can return them later.

---

**User:** Why is the discount formula `(price * 90 + 50) / 100` instead of just `price * 0.9`?

**Assistant:** Integer arithmetic. `getPriceCents()` returns an `int` (cents, not dollars), so you need to stay in integer arithmetic throughout. `price * 0.9` would require a cast to `double` and then back to `int`, which introduces floating-point precision risk — `0.9` can't be represented exactly in binary floating point, so you might get 139 when you expect 140 for a 155-cent item.

The formula `(price * 90 + 50) / 100` does everything in integer arithmetic and rounds to the nearest cent. Breaking it down: multiply by 90 to get "90% of price scaled by 100", add 50 to bias the rounding (half-up), then divide by 100 to get back to cents. For 155 cents: `155 * 90 = 13950`, `13950 + 50 = 14000`, `14000 / 100 = 140`. Without the `+50`: `13950 / 100 = 139` (truncation). The same pattern is used in `VIPCustomer.discountedBill` and `HappyHour` pricing.

---

**User:** Should `ComboMeal` be `final`?

**Assistant:** Yes, same reasoning as `VIPCustomer` and `CriticCustomer` — it's a concrete leaf type with no intended subtypes. Making it `final` also ensures that `getComponents()` returns exactly the two components stored at construction time, without a subclass being able to override that behaviour. Since `ComboMeal` stores its components as private fields and exposes them via `getComponents()`, making the class `final` guarantees the returned list is always consistent with what was passed to the constructor.

---
