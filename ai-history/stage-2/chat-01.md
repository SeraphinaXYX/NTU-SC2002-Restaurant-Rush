# AI Chat History — Inheritance Design for VIPCustomer and CriticCustomer — 15 October 2026

---

**User:** I'm adding VIP and Critic customer types in Stage 2. My first instinct was to add a `customerType` field to the base `Customer` class and check it with if/else. But my teammate says I should use inheritance instead. Can you explain why?

**Assistant:** The if/else approach works but it doesn't scale well. Every time you add a new customer type, you'd need to find every place that checks `customerType` and add a new branch. With inheritance, the type-specific behaviour lives inside the subclass — `GameController`, `Waiter`, and `Cashier` just hold `Customer` references and call methods on them without knowing what type they're dealing with.

For your case, the behaviour that varies by type is: how much patience they lose per turn (`getWaitingLoss()`), what they order (`chooseMeal()`), and what they pay (`discountedBill()`). Make these methods on `Customer` that subtypes can override. `VIPCustomer` overrides `getWaitingLoss()` to return 5 instead of 8, and overrides `discountedBill()` to apply a 10% discount. `CriticCustomer` overrides `getWaitingLoss()` to return 12 and overrides `chooseMeal()` to pick the most demanding item. Everything else — the state machine, patience tracking, payment flow — is inherited unchanged.

This is exactly what polymorphism is for: different objects respond differently to the same method call without the caller knowing their concrete type.

---

**User:** Why override `getWaitingLoss()` as a method rather than just setting a field in the constructor? Like `super(id, arrivalTurn)` and then `this.patienceLoss = 5`?

**Assistant:** You could store it as a field, but overriding the method is cleaner here because `getWaitingLoss()` is called by `losePatience()` which is defined in `Customer`. If you use a field, `losePatience` in the base class can't access a field declared in a subclass — you'd need to either declare the field in the base class (exposing it unnecessarily) or redeclare `losePatience` in every subclass (duplicating logic).

With a method override, `losePatience()` in `Customer` calls `this.getWaitingLoss()`, and since `this` refers to the actual runtime object, Java dispatches to the overriding version in `VIPCustomer` or `CriticCustomer` automatically. No duplication, no field leakage. The base class defines the algorithm (`losePatience`) and delegates the variable part (`getWaitingLoss`) to subclasses — that's the template method pattern.

---

**User:** Does making `VIPCustomer` and `CriticCustomer` final matter?

**Assistant:** It's good practice when you don't intend further subclassing. Marking them `final` communicates intent — these are concrete leaf types, not meant to be extended again — and prevents accidental subclassing that could break the invariants you've built in. For example, if someone subclassed `VIPCustomer` and overrode `discountedBill()` unexpectedly, the game's economic balance could break in ways that are hard to debug.

For a university project, `final` also signals to the marker that you've thought about the class hierarchy and consciously decided these types are terminal. It's a low-cost way to show intentional design. If you later needed a `PlatinumVIPCustomer` subtype, you'd remove `final` deliberately — which forces a conscious decision rather than accidental extension.

---
