# AI Chat History — Understanding Stage 1 Customer/Order State Flow — 13 October 2026

---

**User:** I need to extend the customer system for Stage 2 (adding VIP and Critic types). Before I start, I want to make sure I understand Stage 1 properly. Can you walk me through what happens to a Customer from the moment they arrive to when they pay?

**Assistant:** Sure. A customer arrives when `ArrivalSchedule.arrivalAt(turn)` returns a non-null object — that happens on turns where `(turn - 1) % 3 == 0`, so turns 1, 4, 7, 10, 13, and 16. The customer starts in `CustomerStatus.WAITING`.

From there, the waiter calls `seat(customer, table)`, which calls `customer.seat()` — that transitions to `SEATED`. Then `waiter.takeOrder(customer, menu)` calls `customer.placeOrder(order)`, attaching an `Order` with status `TAKEN`. The chef prepares it via `chef.prepare(customer, turn)`, which advances the order to `READY`. On a later turn, the waiter serves it: `waiter.serve(customer, turn)` calls `customer.serve(turn)`, setting the order to `SERVED` and status to `EATING`, recording `servedTurn`.

At end-of-turn, `customer.finishEating(turn)` checks if `servedTurn < turn` — if so, moves to `READY_TO_PAY`. The cashier then collects: `cashier.collectOldest(customers, turn)` finds the oldest `READY_TO_PAY` customer where `readyToPayTurn < turn`, calls `customer.pay(turn)`, which marks the order `PAID` and returns a `PaymentRecord`.

---

**User:** So the one-turn delay between EATING and READY_TO_PAY, and between READY_TO_PAY and PAID — both are enforced by comparing turn numbers?

**Assistant:** Yes, both use the same `< turn` check rather than `<= turn`. `finishEating` transitions to `READY_TO_PAY` only when `servedTurn < turn`, meaning the customer can't be ready to pay on the same turn they were served. Similarly, `canPay` requires `readyToPayTurn < turn`. The effect is that the minimum end-to-end time from serving to payment is two turns.

This is worth understanding before adding VIP/Critic logic because those timing rules don't change — VIPs and Critics go through the same state machine. What changes is the *rate* at which they lose patience (`getWaitingLoss()`), the *item* they choose (`chooseMeal()`), and optionally the *price* they pay (`discountedBill()`). The state transitions themselves are inherited unchanged from `Customer`.

---

**User:** One more thing — `customer.chooseMeal(menu)` in Stage 1 just picks `items.get((id - 1) % items.size())`. Is that intentional?

**Assistant:** Yes, it's a deterministic selection based on customer ID, which makes the game reproducible. Customer 1 picks item 0, customer 2 picks item 1, and so on, wrapping around with modulo if there are more customers than menu items. It's not a "realistic" choice, but it makes test fixtures predictable — you can write `.in`/`.out` files and know exactly which item every customer will order.

For Stage 2, `CriticCustomer.chooseMeal()` overrides this to pick the most complex (highest `preparationUnits`) item on the menu, with price as a tiebreak. That's a behavioural change, not a structural one — the method signature stays the same, so `Waiter.takeOrder` calls `customer.chooseMeal(menu)` and gets whichever logic applies to that customer type.

---
