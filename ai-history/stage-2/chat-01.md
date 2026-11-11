# AI Chat History — Understanding VIPCustomer Integration — 15 October 2026

---

**User:** My teammate added VIPCustomer and CriticCustomer in Stage 2. I'm trying to understand how their discount works without changing my Stage 1 code. The base Customer has a discountedBill method that just returns baseCents unchanged — is that really enough of a hook?

**Assistant:** Yes, that's the standard template method pattern. The base `Customer.discountedBill(int baseCents)` returns `baseCents` as-is, which means "no discount" for regular customers. `VIPCustomer` overrides it to return `(baseCents * 90 + 50) / 100` — that's a 10% reduction with correct integer rounding (the `+50` rounds to the nearest cent rather than truncating). `CriticCustomer` doesn't override it, so critics pay full price.

The key is that nothing in your Stage 1 code needs to know about subtypes. Wherever `Waiter` or `Cashier` calls `customer.discountedBill(price)`, Java dispatches to whichever override is appropriate at runtime. Your `GameController`, `Waiter`, and `Cashier` all hold `Customer` references — they never cast to `VIPCustomer` — so the discount applies automatically without any changes to Stage 1 classes.

---

**User:** And ArrivalSchedule decides which type arrives on which turn? So customer ID 2 is always VIP?

**Assistant:** Exactly. `ArrivalSchedule.arrivalAt(int turn)` returns null for turns that have no arrival, and constructs the right subtype when a customer does arrive. Customer ID 2 (arriving turn 4) is `new VIPCustomer(2, 4)`, customer ID 3 (arriving turn 7) is `new CriticCustomer(3, 7)`, and the rest are plain `Customer`. The IDs are deterministic — ID is `(turn + 2) / 3` for arrival turns.

This keeps subtype creation in one place. If the schedule changes, you only edit `ArrivalSchedule`, not `GameController`. It also means your Stage 1 `GameController` just calls `arrivals.arrivalAt(turn)` and adds the result to the customer list — the fact that it might be a VIP is invisible at that call site.

---
