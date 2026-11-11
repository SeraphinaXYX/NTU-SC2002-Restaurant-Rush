# AI Chat History — Designing .in/.out Test Fixtures — 6 October 2026

---

**User:** I'm writing the tests for Restaurant Rush. The game is CLI-based so the test runner feeds input line by line and compares output. How should I design the `.in` fixture files so the test cases are deterministic?

**Assistant:** The key is to control every player decision so the same `.in` file always produces the same `.out`. In a turn-based game like this, each line of the `.in` file corresponds to one player input — a command like "seat customer 1", "take order for customer 2", or "wait" for each phase. Because arrivals are deterministic (every 3 turns, same subtypes at the same turns), the game state at each point is fully predictable if the player choices are fixed.

Write your victory and defeat fixtures separately. For a victory `.in`, make choices that maximise throughput — seat customers promptly, serve in order, don't skip turns. For a defeat `.in`, delay deliberately so customers run out of patience. The `.out` file is then the exact output the game should produce, including all prompts, the turn summaries, and the final score line. Generate `.out` by running the game once with the `.in` input you're happy with, then lock it in. After that, any code change that alters the output will cause a mismatch — which is exactly what you want to catch.

---

**User:** How does `RuleChecks` differ from the fixture tests? I'm not sure when to use each.

**Assistant:** Fixture tests (`stage1-victory.in` / `.out`, etc.) are end-to-end: they replay a full game and verify the exact textual output hasn't changed. They catch regressions in the CLI layer, the turn flow, and the formatting all at once, but they're coarse — if the output changes by one character anywhere, the test fails without telling you why.

`RuleChecks` is unit-level: it instantiates `GameController` directly (no CLI) and calls methods programmatically, asserting specific state after each step. For example: "after seating customer 1, their status should be SEATED and table 1 should be occupied." These checks are targeted and fast — they tell you exactly which invariant broke. Write `RuleChecks` assertions for the rules that matter most: state transitions, payment timing, patience loss per turn, and game-over conditions. Use fixture tests to verify the full run; use `RuleChecks` to pinpoint which rule is broken when a fixture fails.

---

**User:** Should I write separate fixture files for each stage or reuse Stage 1 fixtures in Stages 2 and 3?

**Assistant:** Write separate fixtures per stage. The game behaviour changes between stages — Stage 2 adds VIP and Critic customers with different patience rates, Stage 3 adds a Host phase and combo meals — so Stage 1 fixtures would likely fail or give misleading results against Stage 2/3 code.

Name them `stage1-victory.in`, `stage2-victory.in`, `stage3-victory.in`, and so on. For defeat, do the same. This way each stage tag can be verified independently: check out `stage-1`, run the stage 1 fixtures; check out `stage-2`, run the stage 2 fixtures. The briefing requires that each tagged commit compiles and runs correctly, so independent fixtures per stage make that verification straightforward.

---
