# AI Chat History — Customer Satisfaction and Patience Logic — 4 October 2026

---

**User:** For the customer patience system, each customer starts at 100 satisfaction and loses some amount each turn while they're waiting. When satisfaction hits 0 they leave. I'm not sure how to structure this — should I store the patience loss rate as a constructor parameter, or use a method?

**Assistant:** Use a method rather than a stored field. Define `getWaitingLoss()` in `Customer` returning a fixed value like 8, then call it inside `losePatience()`:

```java
public void losePatience() {
    if (!status.equals(CustomerStatus.WAITING) && !status.equals(CustomerStatus.SEATED)) return;
    satisfaction -= getWaitingLoss();
    if (satisfaction < 0) satisfaction = 0;
    if (satisfaction == 0) {
        if (order != null) order.cancel();
        status = CustomerStatus.LEFT;
    }
}
```

The reason to use a method instead of a field is extensibility: when Stage 2 introduces VIP and Critic customers, they can simply override `getWaitingLoss()` to return a different value — no constructor changes, no field injection, no extra parameters. The base `Customer` class doesn't need to know that subtypes exist. If you used a constructor parameter, every subclass would need to pass a different value up to `super(...)`, which works but couples the rate to the construction site rather than the type.

---

**User:** What statuses can a customer be in? I want to make sure I'm not checking the wrong state at the wrong time.

**Assistant:** The full lifecycle is: `WAITING` (arrived, in queue) → `SEATED` (assigned a table) → `EATING` (order served) → `READY_TO_PAY` (finished eating, waiting for cashier) → `PAID` (transaction complete). There's also `LEFT` for customers who ran out of patience before being served.

A few timing rules matter. `finishEating` transitions `EATING → READY_TO_PAY` only if the customer was served on a *previous* turn (`servedTurn < turn`), never the same turn they were served. Similarly, `canPay` requires `readyToPayTurn < turn` — the cashier can't collect on the same turn the customer became ready. These one-turn delays represent natural pacing and also prevent the player from resolving everything in a single turn.

Patience loss only applies while `WAITING` or `SEATED` — a customer who is eating or waiting to pay is no longer at risk of leaving. When a customer does leave (satisfaction reaches 0), cancel any in-progress order so the chef doesn't keep preparing it.

---

**User:** Should I track satisfaction changes in a log somewhere, or just the current value?

**Assistant:** Just the current value is enough for the game logic. The only things that read satisfaction are `losePatience` (to check if the customer leaves), `pay` (to record it in the `PaymentRecord` for the final score), and `recoverSatisfaction` in Stage 3 (Happy Hour boost). None of them need history — they just need the present number.

If you wanted to show the player a satisfaction trend in the UI, you could track deltas, but for a CLI game that display isn't necessary. Keep the model minimal: `int satisfaction`, the mutating methods, and an accessor `getSatisfaction()`. Adding a log now for a feature you may never build would complicate the class without benefit.

---
