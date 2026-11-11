# AI Chat History — Phase State Machine Design — 2 October 2026

---

**User:** I'm building a turn-based restaurant game in Java. Each turn has a fixed sequence: the waiter acts, then the chef, then end-of-turn. I want to enforce this order so the caller can't, say, call `prepareOrder` before `takeOrder`. How should I structure that in `GameController`?

**Assistant:** A lightweight string-based phase guard is a clean fit here. Define a class (or just string constants in a dedicated class) to name each phase — something like `BETWEEN_TURNS`, `WAITER`, `CHEF`, `END_OF_TURN`, and `FINISHED`. Store the current phase as a private field in `GameController`, then write a small helper:

```java
private void requirePhase(String expected) {
    if (!phase.equals(expected)) {
        throw new IllegalStateException("Current phase is " + phase + ".");
    }
}
```

Each public action method starts by calling `requirePhase` with the phase it belongs to, and ends by advancing `phase` to the next one. For example, `takeOrder` calls `requirePhase(TurnPhase.WAITER)` and then sets `phase = TurnPhase.CHEF`. This makes invalid call sequences blow up immediately with a clear message rather than producing silent corruption.

The advantage over an enum is that the error message names the phase as a plain string, which is easy to test against. The advantage over booleans is that you only need one field — there's no combination of flags to reason about.

---

**User:** Makes sense. So `beginTurn` sets it to `WAITER`, and then each step advances it? What about when the game ends after turn 18?

**Assistant:** Exactly. `beginTurn` increments the turn counter and sets `phase = TurnPhase.WAITER` (or whatever the first active phase is). Then `finishTurn` handles the end: after all end-of-turn bookkeeping — eating progress, cashier collection, patience loss, table release — it checks whether `turn == MAX_TURNS`. If so, it sets `phase = TurnPhase.FINISHED` instead of `BETWEEN_TURNS`.

Methods like `getResult()` can then call `requirePhase(TurnPhase.FINISHED)` to ensure nobody reads the final score mid-game. This keeps the terminal state explicit and queryable without a separate `isFinished` boolean — though you can add a convenience method `isFinished()` that just returns `phase.equals(TurnPhase.FINISHED)` for callers that need to loop.

One thing worth locking down: make `GameController` final (or at minimum make `phase` private with no setter) so a subclass can't bypass the guard. The whole point of the pattern is that the transition logic is the only way to move between phases.

---

**User:** What if the waiter has nothing useful to do on a given turn — no customers to seat or serve? Should I skip the WAITER phase entirely?

**Assistant:** Don't skip the phase — instead add a `waitWaiter()` method that calls `requirePhase(TurnPhase.WAITER)` and immediately advances to `TurnPhase.CHEF`. The caller (your CLI layer) decides whether to act or wait; the controller just enforces that one waiter action (even "do nothing") must happen before the chef can act.

This keeps the phase sequence unconditional. If you allowed skipping phases, callers would need to track which phases are active each turn, which leaks turn logic out of `GameController`. With a no-op method, the sequence is always the same: begin → waiter action or wait → chef action or wait → finish. The CLI just picks which one based on the game state it reads from the controller's getters.

---
