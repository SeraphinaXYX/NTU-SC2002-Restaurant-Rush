# AI Chat History — HappyHour Once-Per-Game Constraint and PricingService — 2 November 2026

---

**User:** For HappyHour, the spec says it can only be activated once per game and lasts for 2 turns. I'm tracking `activationTurn` as a field — if it's 0 it hasn't been used, otherwise it holds the turn it was activated. Does that work?

**Assistant:** Yes, that works cleanly. `hasBeenUsed()` just checks `activationTurn != 0`. Then `startTurn` guards the activation:

```java
public void startTurn(int turn, boolean activate) {
    if (activate && hasBeenUsed()) {
        throw new IllegalStateException("Happy Hour can be activated only once.");
    }
    currentTurn = turn;
    if (activate) activationTurn = turn;
}
```

For `getRemainingTurns()`, the duration is 2 turns starting from `activationTurn`:

```java
public int getRemainingTurns() {
    if (!hasBeenUsed()) return 0;
    int remaining = 2 - (currentTurn - activationTurn);
    return remaining < 0 ? 0 : remaining;
}
```

On the activation turn itself (`currentTurn == activationTurn`), `2 - 0 = 2` remaining. The next turn, `2 - 1 = 1`. The turn after, `2 - 2 = 0`. So `isActive()` returns true for exactly the activation turn and the one that follows.

---

**User:** Where does Happy Hour actually affect gameplay — only in PricingService, or elsewhere too?

**Assistant:** Two places. First, `PricingService.quote()` applies a 20% discount to the final bill when `happyHour.isActive()` — that's the pricing effect. Second, `GameController.beginTurn()` calls `customer.recoverSatisfaction(15)` for every current customer when Happy Hour is active — that restores 15 satisfaction points to waiting and seated customers, helping prevent them from leaving mid-game.

The recovery effect is handled in `GameController` rather than in `HappyHour` itself, because recovering satisfaction requires iterating the customer list, which `GameController` owns. `HappyHour` just answers `isActive()` — it doesn't know about customers. This separation keeps `HappyHour` focused on its own state and makes it independently testable.

---

**User:** How does PricingService get the HappyHour reference — does it look it up from GameController?

**Assistant:** No, it receives it via constructor injection. `GameController` creates both `HappyHour` and `PricingService` as fields:

```java
private final HappyHour happyHour = new HappyHour();
private final Waiter waiter = new Waiter(new PricingService(happyHour));
```

`PricingService` stores the reference and queries it when quoting prices. Since both objects are created in `GameController` and share the same `HappyHour` instance, the `PricingService` always sees the current happy hour state — when `happyHour.startTurn(turn, true)` activates it, `happyHour.isActive()` returns true immediately in `PricingService.quote()` on the same turn.

This is dependency injection: `PricingService` doesn't create or locate `HappyHour` itself, it receives it. The benefit for testing is that you can pass a `HappyHour` in any state you want to verify the pricing logic without running a full game.

---
