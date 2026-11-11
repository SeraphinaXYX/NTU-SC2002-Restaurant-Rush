# AI Chat History — Testing VIP and Critic Behaviour — 20 October 2026

---

**User:** Stage 2 adds VIPCustomer (patience loss 5, 10% discount) and CriticCustomer (patience loss 12, picks hardest item). What should my RuleChecks assertions cover for these?

**Assistant:** Test the distinguishing behaviours directly on `GameController`. For VIP patience, run the game to the point where customer 2 (the VIP, arriving turn 4) has been waiting several turns without being seated, then check `getSatisfaction()`. After N patience-loss turns, a VIP should have `100 - (5 * N)` satisfaction, while a regular customer would have `100 - (8 * N)`. Asserting both lets you confirm the override is active.

For the Critic discount — there is none, critics pay full price. But their `chooseMeal` picks the most demanding item. Test this by checking which `MenuItem` appears in the Critic's `Order` after `takeOrder` is called. With a known menu, you can predict exactly which item the Critic selects and assert the order's item name or price matches.

For VIP discount, after the VIP pays, check the `PaymentRecord.getBillCents()` and verify it's 90% of the base item price (rounded). You need to know the VIP's chosen menu item to compute the expected value.

---

**User:** How do I get the PaymentRecord for a specific customer to check the bill?

**Assistant:** `GameController.getPayments()` returns `ArrayList<PaymentRecord>`. Each `PaymentRecord` has `getCustomerId()`, `getBillCents()`, and `getSatisfaction()`. After running the game to completion (or at least to the point where the VIP has paid), iterate the payments list and find the one where `getCustomerId() == 2` (the VIP's ID). Then assert `getBillCents()` equals the expected discounted amount.

In a `RuleChecks` test where you control the entire game sequence programmatically, you know exactly which turn each customer pays — so you can assert the payment list size and the specific record after the payment turn. This is cleaner than testing through the CLI output, because you're asserting a specific data value rather than a formatted string.

---

**User:** The Critic's high patience loss (12 per turn) means they'll leave quickly. Should I write a specific test that verifies a Critic leaves before a regular customer given the same wait?

**Assistant:** Yes, that's a good targeted test. Set up a scenario where you deliberately don't serve customer 3 (the Critic, arriving turn 7). After `100 / 12 = 8` patience-loss turns (turns 8–15, since patience loss happens at end-of-turn), the Critic's satisfaction reaches 4 → they leave before turn 16. A regular customer in the same position would last `100 / 8 = 12` patience-loss turns.

In `RuleChecks`, you can run just enough turns to get past the Critic's departure threshold, then assert `customer.getStatus().equals(CustomerStatus.LEFT)` for the Critic while checking that a regular customer in the same position still has non-zero satisfaction. This confirms that the `getWaitingLoss()` override is wired into `losePatience()` correctly, which is the core invariant of the Stage 2 customer hierarchy.

---
