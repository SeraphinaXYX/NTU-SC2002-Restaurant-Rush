# AI Chat History — Reading Stage 2 Code Before Stage 3 Work — 23 October 2026

---

**User:** I'm about to start Stage 3 (adding Host, ComboMeal, HappyHour). I've been reading the Stage 2 code to understand the current structure. I noticed the turn phases go WAITER → CHEF → END_OF_TURN. Stage 3 adds a Host phase — where does it fit?

**Assistant:** The Host phase slots in before the Waiter phase, making the full sequence: `HOST → WAITER → CHEF → END_OF_TURN`. The host handles seating only — they can assign one waiting customer to a free table per turn. After the host acts (or if there's nothing to do, after a "wait" call), control passes to the waiter as before.

The reason it goes before the waiter is that seating is a prerequisite for taking an order. In Stage 1 and 2, the waiter handled seating directly. Stage 3 introduces a dedicated staff role for it, so the waiter's role narrows to order-taking and serving. The phase order matches the service sequence: seat first, then take order, then prepare, then end of turn.

You'll need to add `HOST` to `TurnPhase`, have `beginTurn` set phase to `HOST` instead of `WAITER`, add `seatWithHost(int id)` and `waitHost()` methods in `GameController`, and each of those advances the phase to `WAITER` when done.

---

**User:** Does the existing Stage 2 code need much change to make room for the Host?

**Assistant:** Mostly just `GameController` and `TurnPhase`. The domain classes — `Customer`, `VIPCustomer`, `CriticCustomer`, `Waiter`, `Chef`, `Cashier` — don't change. `Waiter` loses its seating responsibility (that moves to `Host`), but its interface for `takeOrder` and `serve` stays the same. `Table.seat(customer)` is still the underlying call; `Host.seat` just calls it like `Waiter.seat` did before.

The `Staff` base class (which both `Host` and `Waiter` extend) already has the available/busy logic, so `Host` inherits that and just needs a `seat` method that calls `requireAvailable()`, does `table.seat(customer)`, and then calls `completeTask()`. It's one of the smaller classes in Stage 3.

---
