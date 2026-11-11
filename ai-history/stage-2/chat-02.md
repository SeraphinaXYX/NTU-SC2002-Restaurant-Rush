# AI Chat History — CriticCustomer chooseMeal and discountedBill Hook — 18 October 2026

---

**User:** For CriticCustomer, the spec says they always order the most complex item on the menu. I need to override `chooseMeal(Menu menu)`. My current attempt just sorts the list, but I'm not sure how to handle ties.

**Assistant:** You don't need to sort — a single linear scan is cleaner. Start with the first item as the candidate, then iterate the rest:

```java
public MenuItem chooseMeal(Menu menu) {
    MenuItem selected = menu.getItems().get(0);
    for (MenuItem item : menu.getItems()) {
        if (item.getPreparationUnits() > selected.getPreparationUnits()
                || (item.getPreparationUnits() == selected.getPreparationUnits()
                    && item.getPriceCents() > selected.getPriceCents())) {
            selected = item;
        }
    }
    return selected;
}
```

The tiebreak is price: if two items have equal preparation time, the critic picks the more expensive one. This is deterministic — given the same menu, the critic always picks the same item — which matters for your test fixtures.

The method signature is identical to the base `Customer.chooseMeal(Menu menu)`, so `Waiter.takeOrder` doesn't need to change at all. It calls `customer.chooseMeal(menu)` and Java dispatches to this override for `CriticCustomer` instances.

---

**User:** And for `discountedBill`, how should the base class hook work? The base Customer just returns baseCents unchanged — should I add an abstract method instead?

**Assistant:** Keep it as a concrete method with a default implementation, not abstract. Making it abstract would force every subclass to override it, including `CriticCustomer` which doesn't apply any discount (critics pay full price). An abstract method is appropriate when every subclass *must* provide a different implementation — here, the "no discount" case is a valid, meaningful default.

The pattern is called a hook method: the base class defines a do-nothing (or identity) implementation that subclasses can optionally override. `Customer.discountedBill(int baseCents)` returns `baseCents` — that's the hook. `VIPCustomer` overrides it to apply 10% off:

```java
public int discountedBill(int baseCents) {
    return (baseCents * 90 + 50) / 100;
}
```

The `+50` before dividing by 100 rounds to the nearest cent rather than truncating. For example, 155 cents × 90% = 139.5 cents → with truncation you'd get 139, with rounding you get 140. Using `(x * 90 + 50) / 100` is the standard integer rounding idiom in Java.

---

**User:** Where is `discountedBill` actually called? I want to make sure my Stage 2 addition integrates with Stage 1 without touching Stage 1 files.

**Assistant:** In Stage 2, `discountedBill` is called inside `Waiter` when it records the order price — the waiter quotes `customer.discountedBill(item.getPriceCents())` and uses that as the bill amount in the `Order`. This call site already exists or is added in Stage 2; Stage 1's `Waiter` would have used `item.getPriceCents()` directly.

In Stage 3, `PricingService.quote()` takes over this responsibility: it calls `customer.discountedBill(item.getPriceCents())` first, then applies the Happy Hour multiplier on top. So your hook method works correctly in both stages without modification.

The integration point you want to verify is that no Stage 1 file needs changing. `Customer` already has `discountedBill` as a hook returning `baseCents`, so if that was added in Stage 1 as a placeholder, Stage 2 just adds the overriding subclasses. If `discountedBill` wasn't in Stage 1 at all, then Stage 2 adds it to the base class as a hook and also adds the overrides — that's one file added to `Customer.java`, which counts as a Stage 2 modification, not a Stage 1 violation.

---
