# AI Chat History — Adding the Host Phase to GameController — 26 October 2026

---

**User:** I'm adding the Host to Stage 3. I've got `Host extends Staff` with a `seat` method. Now I need to wire it into `GameController`. Should I add a new field `private final Host host` and call it like the other staff, or is there a different pattern?

**Assistant:** Yes, add `private final Host host = new Host()` as a field alongside `waiter`, `chef`, and `cashier`. Then `beginTurn` should call `host.startTurn()` along with the others and set `phase = TurnPhase.HOST`. You'll add two new public methods:

```java
public void seatWithHost(int id) {
    requirePhase(TurnPhase.HOST);
    host.seat(customer(id), freeTable());
    phase = TurnPhase.WAITER;
}

public void waitHost() {
    requirePhase(TurnPhase.HOST);
    phase = TurnPhase.WAITER;
}
```

Both advance the phase to `WAITER` when done — `seatWithHost` does it after seating, `waitHost` does it immediately. The CLI layer decides which to call based on whether there's a waiting customer and a free table. `GameController` doesn't decide — it just enforces that one of the two is called before the waiter can act.

---

**User:** Why does `waitHost()` exist? Can't the CLI just skip the host if there's nothing to do?

**Assistant:** The CLI can't skip the phase because `takeOrder` calls `requirePhase(TurnPhase.WAITER)` and will throw if the phase is still `HOST`. Every phase needs an explicit advance — that's the contract the state machine enforces. `waitHost()` is the "do nothing but advance" option, the same as `waitWaiter()` and `waitChef()`.

This design keeps the game loop predictable: every turn always goes through all phases, in order, with no implicit skipping. It makes the CLI simpler too — the CLI always calls exactly one host action, one waiter action, one chef action, then finishTurn. There's no need to track which phases are "active" on a given turn.

---

**User:** What if the CLI tries to call `seatWithHost` when there are no free tables? Should `GameController` guard against that?

**Assistant:** The guard already exists in `freeTable()` — it throws `IllegalStateException("No free table.")` if all tables are occupied. So `GameController` will blow up with a clear message if the caller passes an ID but there's nowhere to seat them. The CLI should check `getTables()` before calling `seatWithHost` to avoid that, but the defensive check is already there if it doesn't.

Similarly, `customer(int id)` throws `IllegalArgumentException` if no customer with that ID has arrived. So both "who" and "where" are validated inside the controller. The CLI's job is to present sensible choices to the player; the controller's job is to reject invalid ones. This division is clean and means you don't need duplicate validation in both layers.

---
