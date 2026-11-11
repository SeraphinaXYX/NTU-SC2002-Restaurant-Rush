# AI Chat History — Sequence Diagram for VIP + Happy Hour Scenario — 3 November 2026

---

**User:** I need to draw a sequence diagram for a Stage 3 scenario. I want to show a VIP customer arriving, Happy Hour being activated, and the VIP paying at a discounted price. Which objects should participate?

**Assistant:** For this scenario, the relevant objects are: `GameCLI` (the boundary, initiates each turn), `GameController` (the control object), `HappyHour`, `PricingService`, `VIPCustomer`, `Waiter`, and `Cashier`. You can also include `Table` and `Order` if you want to show the full seating and ordering flow, but keeping it focused on the discount chain makes the diagram more readable.

The interesting sequence to show is: `GameCLI` calls `GameController.beginTurn(true)` with the activate flag → `GameController` calls `happyHour.startTurn(turn, true)` → then when the waiter quotes the price, `GameController` calls `waiter.takeOrder(customer, menu)` → `Waiter` calls `pricingService.quote(customer, item)` → `PricingService` calls `customer.discountedBill(baseCents)` (dispatches to `VIPCustomer`'s override returning 90% of base) → `PricingService` calls `happyHour.isActive()` returning true → applies 20% reduction → returns the final bill amount.

This shows two levels of polymorphic dispatch in one flow, which is exactly what a good Stage 3 sequence diagram should demonstrate.

---

**User:** How do I indicate in the diagram that `discountedBill` dispatches polymorphically to VIPCustomer's override?

**Assistant:** In UML sequence diagrams, the lifeline is typed to the declared reference type, but you show the actual object's class in the lifeline header. Write the lifeline as `: VIPCustomer` (not `: Customer`) to make clear that the concrete type is what's executing. Then label the message `discountedBill(baseCents)` sent from `PricingService` to the `VIPCustomer` lifeline. The return value annotation can say `discountedCents = baseCents * 90 / 100` to document the behaviour.

For the guard conditions, add an `alt` fragment around the Happy Hour discount: one branch labelled `[happyHour.isActive() = true]` showing the 20% reduction, and an `else` branch showing the undiscounted price returned. This makes the conditional logic explicit, which the briefing specifically asks for ("include relevant guards, alternatives or loops").

---

**User:** What starting condition and outcome should I state for this diagram?

**Assistant:** Starting condition: "Turn N where N ≥ 4 (VIP customer 2 has arrived and been seated), Happy Hour has not yet been used, and the player chooses to activate it this turn."

Outcome: "VIP customer's order is priced at `(baseCents × 90 + 50) / 100 × 80 / 100` (approximately 72% of base price). `HappyHour.hasBeenUsed()` becomes true; `getRemainingTurns()` returns 2."

Stating these explicitly in the diagram notes or in the report section that introduces the diagram satisfies the briefing requirement to "state its stage, starting conditions and outcome." It also makes the diagram self-contained — a reader doesn't need to trace through the code to understand what precondition enables the scenario or what the diagram is meant to prove.

---
